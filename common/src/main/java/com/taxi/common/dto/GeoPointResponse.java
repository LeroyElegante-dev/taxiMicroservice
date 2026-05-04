package com.taxi.common.dto;

import java.math.BigDecimal;

public record GeoPointResponse(BigDecimal latitude, BigDecimal longitude) {
}
