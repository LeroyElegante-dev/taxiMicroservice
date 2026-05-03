package com.taxi.common.dto;

import com.taxi.common.model.DriverStatus;
import jakarta.validation.constraints.NotNull;

public record DriverStatusUpdateRequest(
        @NotNull DriverStatus status
) {
}
