package com.taxi.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RecipientType {
    PASSENGER,
    DRIVER,
    BOTH;

    @JsonCreator
    public static RecipientType fromString(String value) {
        if (value == null) {
            return null;
        }
        return RecipientType.valueOf(value.trim().toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
