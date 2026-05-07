package com.taxi.tripservice.repository;

import com.taxi.tripservice.entity.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<TripEntity, Long> {

    List<TripEntity> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    Optional<TripEntity> findByClientRequestId(UUID clientRequestId);
}
