package com.taxi.common.dto;

import java.time.Instant;

public record NotificationTaskResponse(
        Long id,
        Long tripId,
        RecipientType recipientType,
        String message,
        String status,
        int retryCount,
        Instant createdAt,
        Instant updatedAt
) {
}
