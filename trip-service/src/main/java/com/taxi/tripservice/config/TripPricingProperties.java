package com.taxi.tripservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "taxi.trip")
public class TripPricingProperties {

    /**
     * Тариф за один километр «по прямой» (Haversine).
     */
    private BigDecimal pricePerKm = new BigDecimal("50.00");

    public BigDecimal getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(BigDecimal pricePerKm) {
        this.pricePerKm = pricePerKm;
    }
}
