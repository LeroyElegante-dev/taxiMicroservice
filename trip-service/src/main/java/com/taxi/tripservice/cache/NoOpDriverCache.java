package com.taxi.tripservice.cache;

import com.taxi.common.dto.DriverResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "taxi.redis.enabled", havingValue = "false")
public class NoOpDriverCache implements DriverCache {

    @Override
    public Optional<List<DriverResponse>> getFreeDrivers() {
        return Optional.empty();
    }

    @Override
    public void putFreeDrivers(List<DriverResponse> drivers) {
        // no-op
    }

    @Override
    public void invalidateFreeDrivers() {
        // no-op
    }
}
