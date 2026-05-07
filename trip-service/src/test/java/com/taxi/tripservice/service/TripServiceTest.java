package com.taxi.tripservice.service;

import com.taxi.common.dto.GeoPointRequest;
import com.taxi.common.dto.PassengerResponse;
import com.taxi.common.dto.TripCreateRequest;
import com.taxi.common.dto.TripRatingPatchRequest;
import com.taxi.common.dto.TripStatusPatchRequest;
import com.taxi.common.exception.DuplicateResourceException;
import com.taxi.common.exception.ResourceNotFoundException;
import com.taxi.common.model.DriverStatus;
import com.taxi.common.model.TripStatus;
import com.taxi.tripservice.config.TripPricingProperties;
import com.taxi.tripservice.entity.TripEntity;
import com.taxi.tripservice.integration.DriverServiceClient;
import com.taxi.tripservice.integration.NotificationClient;
import com.taxi.tripservice.repository.DriverClaimRepository;
import com.taxi.tripservice.repository.DriverStatusRepository;
import com.taxi.tripservice.repository.TripRepository;
import com.taxi.tripservice.support.InvalidTripOperationException;
import com.taxi.tripservice.support.NoDriversAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    TripRepository tripRepository;
    @Mock
    DriverClaimRepository driverClaimRepository;
    @Mock
    DriverStatusRepository driverStatusRepository;
    @Mock
    DriverServiceClient driverServiceClient;
    @Mock
    NotificationClient notificationClient;
    @Mock
    TransactionTemplate transactionTemplate;

    TripPricingProperties pricingProperties;
    TripService tripService;

    @BeforeEach
    void setUp() {
        pricingProperties = new TripPricingProperties();
        pricingProperties.setPricePerKm(new BigDecimal("10.00"));
        tripService = new TripService(
                tripRepository,
                driverClaimRepository,
                driverStatusRepository,
                driverServiceClient,
                notificationClient,
                pricingProperties,
                transactionTemplate
        );
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            TransactionCallback<TripEntity> cb = invocation.getArgument(0);
            return cb.doInTransaction(new SimpleTransactionStatus());
        });
    }

    @Test
    void createTrip_assignsDriverSyncsBusyAndNotifies() {
        when(driverServiceClient.getPassenger(1L)).thenReturn(
                new PassengerResponse(1L, "P", "p@p", "1", Instant.parse("2026-01-01T00:00:00Z"))
        );
        when(driverServiceClient.listFreeDrivers()).thenReturn(List.of());
        when(driverClaimRepository.claimNextFreeDriver()).thenReturn(Optional.of(9L));
        when(tripRepository.save(any(TripEntity.class))).thenAnswer(inv -> {
            TripEntity t = inv.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 100L);
            ReflectionTestUtils.setField(t, "createdAt", Instant.parse("2026-01-01T12:00:00Z"));
            ReflectionTestUtils.setField(t, "updatedAt", Instant.parse("2026-01-01T12:00:00Z"));
            return t;
        });

        TripCreateRequest req = new TripCreateRequest(
                1L,
                new GeoPointRequest(new BigDecimal("55.75"), new BigDecimal("37.62")),
                new GeoPointRequest(new BigDecimal("55.76"), new BigDecimal("37.63"))
        );

        UUID key = UUID.randomUUID();
        var response = tripService.createTrip(req, key);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.driverId()).isEqualTo(9L);
        assertThat(response.status()).isEqualTo(TripStatus.ASSIGNED);
        assertThat(response.price()).isPositive();

        verify(driverServiceClient).updateDriverStatus(9L, DriverStatus.BUSY);
        verify(notificationClient).onTripStatusChanged(100L, TripStatus.ASSIGNED, "Водитель назначен");
    }

    @Test
    void createTrip_throwsWhenNoFreeDriver() {
        when(driverServiceClient.getPassenger(1L)).thenReturn(
                new PassengerResponse(1L, "P", "p@p", "1", Instant.now())
        );
        when(driverServiceClient.listFreeDrivers()).thenReturn(List.of());
        when(driverClaimRepository.claimNextFreeDriver()).thenReturn(Optional.empty());

        TripCreateRequest req = new TripCreateRequest(
                1L,
                new GeoPointRequest(new BigDecimal("0"), new BigDecimal("0")),
                new GeoPointRequest(new BigDecimal("0"), new BigDecimal("1"))
        );

        assertThatThrownBy(() -> tripService.createTrip(req, UUID.randomUUID()))
                .isInstanceOf(NoDriversAvailableException.class);
    }

    @Test
    void createTrip_idempotencyReturnsExistingTripWithoutSideEffects() {
        UUID key = UUID.randomUUID();
        TripEntity existing = tripEntity(TripStatus.ASSIGNED);
        ReflectionTestUtils.setField(existing, "id", 777L);
        when(tripRepository.findByClientRequestId(key)).thenReturn(Optional.of(existing));

        TripCreateRequest req = new TripCreateRequest(
                1L,
                new GeoPointRequest(new BigDecimal("55.75"), new BigDecimal("37.62")),
                new GeoPointRequest(new BigDecimal("55.76"), new BigDecimal("37.63"))
        );

        var out = tripService.createTrip(req, key);

        assertThat(out.id()).isEqualTo(777L);
        verify(driverServiceClient, never()).getPassenger(anyLong());
        verify(driverClaimRepository, never()).claimNextFreeDriver();
        verify(tripRepository, never()).save(any(TripEntity.class));
        verify(notificationClient, never()).onTripStatusChanged(anyLong(), any(), any());
    }

    @Test
    void getById_throwsWhenMissing() {
        when(tripRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> tripService.getById(7L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("7");
    }

    @Test
    void updateStatus_assignedToAccepted() {
        TripEntity trip = tripEntity(TripStatus.ASSIGNED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(TripEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var out = tripService.updateStatus(1L, new TripStatusPatchRequest(TripStatus.ACCEPTED));

        assertThat(out.status()).isEqualTo(TripStatus.ACCEPTED);
        verify(notificationClient).onTripStatusChanged(1L, TripStatus.ACCEPTED, "Статус поездки обновлён");
        verify(driverServiceClient, never()).updateDriverStatus(anyLong(), eq(DriverStatus.FREE));
    }

    @Test
    void updateStatus_completedFreesDriver() {
        TripEntity trip = tripEntity(TripStatus.IN_PROGRESS);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(TripEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        tripService.updateStatus(1L, new TripStatusPatchRequest(TripStatus.COMPLETED));

        verify(driverStatusRepository).setStatus(2L, DriverStatus.FREE);
        verify(driverServiceClient).updateDriverStatus(2L, DriverStatus.FREE);
        ArgumentCaptor<TripStatus> cap = ArgumentCaptor.forClass(TripStatus.class);
        verify(notificationClient).onTripStatusChanged(eq(1L), cap.capture(), eq("Статус поездки обновлён"));
        assertThat(cap.getValue()).isEqualTo(TripStatus.COMPLETED);
    }

    @Test
    void updateStatus_invalidTransition() {
        TripEntity trip = tripEntity(TripStatus.ASSIGNED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> tripService.updateStatus(1L, new TripStatusPatchRequest(TripStatus.IN_PROGRESS)))
                .isInstanceOf(InvalidTripOperationException.class)
                .hasMessageContaining("ASSIGNED");
    }

    @Test
    void updateRating_onlyAfterCompleted() {
        TripEntity trip = tripEntity(TripStatus.ACCEPTED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> tripService.updateRating(1L, new TripRatingPatchRequest(5)))
                .isInstanceOf(InvalidTripOperationException.class);
    }

    @Test
    void updateRating_twiceThrowsConflict() {
        TripEntity trip = tripEntity(TripStatus.COMPLETED);
        trip.setRating(4);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        assertThatThrownBy(() -> tripService.updateRating(1L, new TripRatingPatchRequest(5)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateRating_success() {
        TripEntity trip = tripEntity(TripStatus.COMPLETED);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(any(TripEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var out = tripService.updateRating(1L, new TripRatingPatchRequest(5));

        assertThat(out.rating()).isEqualTo(5);
        verify(notificationClient).onTripStatusChanged(1L, TripStatus.COMPLETED, "Пассажир поставил оценку");
    }

    @Test
    void listForPassenger_returnsMappedRows() {
        TripEntity a = tripEntity(TripStatus.COMPLETED);
        ReflectionTestUtils.setField(a, "id", 1L);
        TripEntity b = tripEntity(TripStatus.ASSIGNED);
        ReflectionTestUtils.setField(b, "id", 2L);
        when(tripRepository.findByPassengerIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(a, b));

        var list = tripService.listForPassenger(5L);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).id()).isEqualTo(1L);
    }

    private static TripEntity tripEntity(TripStatus status) {
        TripEntity t = new TripEntity();
        ReflectionTestUtils.setField(t, "id", 1L);
        t.setPassengerId(5L);
        t.setDriverId(2L);
        t.setStatus(status);
        t.setOriginLat(new BigDecimal("55.75"));
        t.setOriginLng(new BigDecimal("37.62"));
        t.setDestLat(new BigDecimal("55.76"));
        t.setDestLng(new BigDecimal("37.63"));
        t.setPrice(new BigDecimal("10.00"));
        ReflectionTestUtils.setField(t, "createdAt", Instant.parse("2026-01-01T00:00:00Z"));
        ReflectionTestUtils.setField(t, "updatedAt", Instant.parse("2026-01-01T00:00:00Z"));
        return t;
    }
}
