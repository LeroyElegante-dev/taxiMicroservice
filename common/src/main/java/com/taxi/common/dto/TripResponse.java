package com.taxi.common.dto;

import com.taxi.common.model.TripStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TripResponse(
        Long id,
        Long passengerId,
        Long driverId,
        TripStatus status,
        GeoPointResponse origin,
        GeoPointResponse destination,
        BigDecimal price,
        Integer rating,
        Instant createdAt,
        Instant updatedAt
) {
}
