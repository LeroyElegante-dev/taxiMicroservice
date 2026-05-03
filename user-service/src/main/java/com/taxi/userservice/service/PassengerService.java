package com.taxi.userservice.service;

import com.taxi.common.dto.PassengerRegistrationRequest;
import com.taxi.common.dto.PassengerResponse;
import com.taxi.common.exception.DuplicateResourceException;
import com.taxi.common.exception.ResourceNotFoundException;
import com.taxi.userservice.entity.PassengerEntity;
import com.taxi.userservice.repository.PassengerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Transactional
    public PassengerResponse register(PassengerRegistrationRequest request) {
        if (passengerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Пассажир с таким email уже зарегистрирован");
        }
        PassengerEntity entity = new PassengerEntity();
        entity.setName(request.name().trim());
        entity.setEmail(request.email().trim().toLowerCase());
        entity.setPhone(request.phone().trim());
        PassengerEntity saved = passengerRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PassengerResponse getById(Long id) {
        PassengerEntity entity = passengerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пассажир с id=" + id + " не найден"));
        return toResponse(entity);
    }

    private static PassengerResponse toResponse(PassengerEntity entity) {
        return new PassengerResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCreatedAt()
        );
    }
}
