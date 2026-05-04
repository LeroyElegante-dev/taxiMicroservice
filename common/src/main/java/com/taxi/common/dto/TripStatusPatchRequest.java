package com.taxi.common.dto;

import com.taxi.common.model.TripStatus;
import jakarta.validation.constraints.NotNull;

public record TripStatusPatchRequest(@NotNull TripStatus status) {
}
