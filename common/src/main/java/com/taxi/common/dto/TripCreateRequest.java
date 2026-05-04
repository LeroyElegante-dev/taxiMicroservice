package com.taxi.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TripCreateRequest(
        @NotNull Long passengerId,
        @NotNull @Valid GeoPointRequest origin,
        @NotNull @Valid GeoPointRequest destination
) {
}
