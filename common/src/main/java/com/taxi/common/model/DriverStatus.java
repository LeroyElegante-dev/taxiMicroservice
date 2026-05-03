package com.taxi.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DriverStatus {
    FREE,
    BUSY,
    OFFLINE;

    @JsonCreator
    public static DriverStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        return DriverStatus.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
