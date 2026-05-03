package com.taxi.userservice.repository;

import com.taxi.common.model.DriverStatus;
import com.taxi.userservice.entity.DriverEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<DriverEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);

    List<DriverEntity> findByStatus(DriverStatus status);
}
