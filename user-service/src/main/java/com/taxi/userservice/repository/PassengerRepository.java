package com.taxi.userservice.repository;

import com.taxi.userservice.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassengerRepository extends JpaRepository<PassengerEntity, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
