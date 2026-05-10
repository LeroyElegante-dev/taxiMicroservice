package com.taxi.common.dto;

import java.math.BigDecimal;
import java.util.List;

public record DailyTripStatsResponse(
        String date,
        long tripCount,
        BigDecimal averagePrice,
        List<PopularRouteStat> popularRoutes
) {
    public record PopularRouteStat(
            BigDecimal originLat,
            BigDecimal originLng,
            BigDecimal destLat,
            BigDecimal destLng,
            long tripCount
    ) {
    }
}
