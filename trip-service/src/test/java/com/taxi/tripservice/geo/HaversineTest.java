package com.taxi.tripservice.geo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HaversineTest {

    @Test
    void moscowShortHopIsSmallDistance() {
        double km = Haversine.distanceKm(
                new BigDecimal("55.7558"),
                new BigDecimal("37.6173"),
                new BigDecimal("55.7658"),
                new BigDecimal("37.6273")
        );
        assertThat(km).isGreaterThan(0.5).isLessThan(2.0);
    }
}
