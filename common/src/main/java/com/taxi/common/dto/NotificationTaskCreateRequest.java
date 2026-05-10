package com.taxi.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationTaskCreateRequest(
        @NotNull Long tripId,
        @NotNull RecipientType recipientType,
        @NotBlank String message
) {
}
