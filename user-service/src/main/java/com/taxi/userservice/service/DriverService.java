package com.taxi.userservice.service;

import com.taxi.common.dto.DriverRegistrationRequest;
import com.taxi.common.dto.DriverResponse;
import com.taxi.common.dto.DriverStatusUpdateRequest;
import com.taxi.common.exception.DuplicateResourceException;
import com.taxi.common.exception.ResourceNotFoundException;
import com.taxi.common.model.DriverStatus;
import com.taxi.userservice.entity.DriverEntity;
import com.taxi.userservice.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public DriverResponse register(DriverRegistrationRequest request) {
        if (driverRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Водитель с таким email уже зарегистрирован");
        }
        DriverEntity entity = new DriverEntity();
        entity.setName(request.name().trim());
        entity.setEmail(request.email().trim().toLowerCase());
        entity.setPhone(request.phone().trim());
        entity.setLicenseNumber(request.licenseNumber().trim());
        entity.setStatus(request.status() != null ? request.status() : DriverStatus.FREE);
        DriverEntity saved = driverRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DriverResponse getById(Long id) {
        DriverEntity entity = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Водитель с id=" + id + " не найден"));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> list(DriverStatus statusFilter) {
        if (statusFilter == null) {
            return driverRepository.findAll().stream().map(DriverService::toResponse).toList();
        }
        return driverRepository.findByStatus(statusFilter).stream().map(DriverService::toResponse).toList();
    }

    @Transactional
    public DriverResponse updateStatus(Long id, DriverStatusUpdateRequest request) {
        DriverEntity entity = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Водитель с id=" + id + " не найден"));
        entity.setStatus(request.status());
        return toResponse(entity);
    }

    private static DriverResponse toResponse(DriverEntity entity) {
        return new DriverResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getLicenseNumber(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
