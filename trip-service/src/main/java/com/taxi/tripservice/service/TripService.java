package com.taxi.tripservice.service;

import com.taxi.common.dto.DriverResponse;
import com.taxi.common.dto.GeoPointRequest;
import com.taxi.common.dto.GeoPointResponse;
import com.taxi.common.dto.TripCreateRequest;
import com.taxi.common.dto.TripRatingPatchRequest;
import com.taxi.common.dto.TripResponse;
import com.taxi.common.dto.TripStatusPatchRequest;
import com.taxi.common.exception.DuplicateResourceException;
import com.taxi.common.exception.ResourceNotFoundException;
import com.taxi.common.model.DriverStatus;
import com.taxi.common.model.TripStatus;
import com.taxi.tripservice.config.TripPricingProperties;
import com.taxi.tripservice.entity.TripEntity;
import com.taxi.tripservice.geo.Haversine;
import com.taxi.tripservice.cache.DriverCache;
import com.taxi.tripservice.integration.DriverServiceClient;
import com.taxi.tripservice.integration.NotificationClient;
import com.taxi.tripservice.repository.DriverClaimRepository;
import com.taxi.tripservice.repository.DriverStatusRepository;
import com.taxi.tripservice.repository.TripRepository;
import com.taxi.tripservice.support.InvalidTripOperationException;
import com.taxi.tripservice.support.NoDriversAvailableException;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private final TripRepository tripRepository;
    private final DriverClaimRepository driverClaimRepository;
    private final DriverStatusRepository driverStatusRepository;
    private final DriverServiceClient driverServiceClient;
    private final NotificationClient notificationClient;
    private final TripPricingProperties pricingProperties;
    private final TransactionTemplate transactionTemplate;
    private final DriverCache driverCache;

    public TripService(
            TripRepository tripRepository,
            DriverClaimRepository driverClaimRepository,
            DriverStatusRepository driverStatusRepository,
            DriverServiceClient driverServiceClient,
            NotificationClient notificationClient,
            TripPricingProperties pricingProperties,
            TransactionTemplate transactionTemplate,
            DriverCache driverCache
    ) {
        this.tripRepository = tripRepository;
        this.driverClaimRepository = driverClaimRepository;
        this.driverStatusRepository = driverStatusRepository;
        this.driverServiceClient = driverServiceClient;
        this.notificationClient = notificationClient;
        this.pricingProperties = pricingProperties;
        this.transactionTemplate = transactionTemplate;
        this.driverCache = driverCache;
    }

    public TripResponse createTrip(TripCreateRequest request, UUID clientRequestId) {
        if (clientRequestId != null) {
            Optional<TripEntity> existing = tripRepository.findByClientRequestId(clientRequestId);
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }

        validatePassenger(request.passengerId());
        List<DriverResponse> preview = driverServiceClient.listFreeDrivers();
        log.info("Перед назначением: GET /drivers?status=FREE вернул {} записей", preview.size());

        BigDecimal price = calculatePrice(request.origin(), request.destination());

        TripEntity saved;
        try {
            saved = transactionTemplate.execute(status -> {
                Optional<Long> driverId = driverClaimRepository.claimNextFreeDriver();
                if (driverId.isEmpty()) {
                    throw new NoDriversAvailableException("Нет свободных водителей");
                }
                TripEntity trip = new TripEntity();
                trip.setClientRequestId(clientRequestId);
                trip.setPassengerId(request.passengerId());
                trip.setDriverId(driverId.get());
                trip.setStatus(TripStatus.ASSIGNED);
                trip.setOriginLat(request.origin().latitude());
                trip.setOriginLng(request.origin().longitude());
                trip.setDestLat(request.destination().latitude());
                trip.setDestLng(request.destination().longitude());
                trip.setPrice(price);
                return tripRepository.save(trip);
            });
        } catch (DataIntegrityViolationException e) {
            if (clientRequestId != null) {
                return tripRepository.findByClientRequestId(clientRequestId)
                        .map(this::toResponse)
                        .orElseThrow(() -> e);
            }
            throw e;
        }
        if (saved == null) {
            throw new IllegalStateException("Не удалось сохранить поездку");
        }

        driverServiceClient.updateDriverStatus(saved.getDriverId(), DriverStatus.BUSY);
        driverCache.invalidateFreeDrivers();
        notificationClient.onTripStatusChanged(saved.getId(), TripStatus.ASSIGNED, "Водитель назначен");

        return toResponse(saved);
    }

    public TripResponse createTrip(TripCreateRequest request) {
        return createTrip(request, null);
    }

    @Transactional(readOnly = true)
    public TripResponse getById(long id) {
        return toResponse(loadTrip(id));
    }

    @Transactional(readOnly = true)
    public List<TripResponse> listForPassenger(long passengerId) {
        return tripRepository.findByPassengerIdOrderByCreatedAtDesc(passengerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TripResponse updateStatus(long id, TripStatusPatchRequest request) {
        TripEntity trip = loadTrip(id);
        TripStatus next = request.status();
        assertTransition(trip.getStatus(), next);
        trip.setStatus(next);
        TripEntity saved = tripRepository.save(trip);

        if (next == TripStatus.COMPLETED || next == TripStatus.CANCELLED) {
            // Гарантируем возврат статуса в БД даже при проблемах связи с User Service
            driverStatusRepository.setStatus(saved.getDriverId(), DriverStatus.FREE);
            // Синхронизация через HTTP — best effort (логирует при сбоях)
            driverServiceClient.updateDriverStatus(saved.getDriverId(), DriverStatus.FREE);
            driverCache.invalidateFreeDrivers();
        }
        notificationClient.onTripStatusChanged(saved.getId(), next, "Статус поездки обновлён");
        return toResponse(saved);
    }

    @Transactional
    public TripResponse updateRating(long id, TripRatingPatchRequest request) {
        TripEntity trip = loadTrip(id);
        if (trip.getStatus() != TripStatus.COMPLETED) {
            throw new InvalidTripOperationException("Оценку можно выставить только после завершения поездки");
        }
        if (trip.getRating() != null) {
            throw new DuplicateResourceException("Оценка для этой поездки уже задана");
        }
        trip.setRating(request.rating());
        TripEntity saved = tripRepository.save(trip);
        notificationClient.onTripStatusChanged(saved.getId(), TripStatus.COMPLETED, "Пассажир поставил оценку");
        return toResponse(saved);
    }

    private TripEntity loadTrip(long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Поездка с id=" + id + " не найдена"));
    }

    private void validatePassenger(long passengerId) {
        try {
            driverServiceClient.getPassenger(passengerId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Пассажир с id=" + passengerId + " не найден");
        }
    }

    private BigDecimal calculatePrice(GeoPointRequest origin, GeoPointRequest destination) {
        double km = Haversine.distanceKm(
                origin.latitude(),
                origin.longitude(),
                destination.latitude(),
                destination.longitude()
        );
        return BigDecimal.valueOf(km)
                .multiply(pricingProperties.getPricePerKm())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static void assertTransition(TripStatus current, TripStatus next) {
        boolean ok = switch (current) {
            case ASSIGNED -> next == TripStatus.ACCEPTED || next == TripStatus.CANCELLED;
            case ACCEPTED -> next == TripStatus.IN_PROGRESS || next == TripStatus.CANCELLED;
            case IN_PROGRESS -> next == TripStatus.COMPLETED || next == TripStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!ok) {
            throw new InvalidTripOperationException(
                    "Недопустимый переход статуса: " + current + " -> " + next
            );
        }
    }

    private TripResponse toResponse(TripEntity e) {
        return new TripResponse(
                e.getId(),
                e.getPassengerId(),
                e.getDriverId(),
                e.getStatus(),
                new GeoPointResponse(e.getOriginLat(), e.getOriginLng()),
                new GeoPointResponse(e.getDestLat(), e.getDestLng()),
                e.getPrice(),
                e.getRating(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
