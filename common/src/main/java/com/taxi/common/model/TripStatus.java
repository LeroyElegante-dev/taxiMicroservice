package com.taxi.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TripStatus {
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    @JsonCreator
    public static TripStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        return TripStatus.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
