package com.taxi.tripservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "taxi.redis")
public class TaxiRedisProperties {

    /** TTL кэша списка свободных водителей, секунды. */
    private int ttlSeconds = 30;

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
