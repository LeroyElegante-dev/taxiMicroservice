package com.taxi.tripservice.geo;

import java.math.BigDecimal;

public final class Haversine {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private Haversine() {
    }

    public static double distanceKm(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        double r1 = Math.toRadians(lat1.doubleValue());
        double r2 = Math.toRadians(lat2.doubleValue());
        double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double dLon = Math.toRadians(lon2.subtract(lon1).doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(r1) * Math.cos(r2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
