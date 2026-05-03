package com.taxi.common.dto;

import com.taxi.common.model.DriverStatus;

import java.time.Instant;

public record DriverResponse(
        Long id,
        String name,
        String email,
        String phone,
        String licenseNumber,
        DriverStatus status,
        Instant createdAt
) {
}
