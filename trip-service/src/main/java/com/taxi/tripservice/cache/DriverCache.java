package com.taxi.tripservice.cache;

import com.taxi.common.dto.DriverResponse;

import java.util.List;
import java.util.Optional;

public interface DriverCache {

    Optional<List<DriverResponse>> getFreeDrivers();

    void putFreeDrivers(List<DriverResponse> drivers);

    void invalidateFreeDrivers();
}
