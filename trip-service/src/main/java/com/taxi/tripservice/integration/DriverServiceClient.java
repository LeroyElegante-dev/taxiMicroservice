package com.taxi.tripservice.integration;

import com.taxi.common.dto.DriverResponse;
import com.taxi.common.dto.PassengerResponse;
import com.taxi.common.model.DriverStatus;

import java.util.List;

/**
 * Синхронные вызовы User Service. Третий участник сможет подменить реализацию кэширующим прокси.
 */
public interface DriverServiceClient {

    PassengerResponse getPassenger(long passengerId);

    List<DriverResponse> listFreeDrivers();

    void updateDriverStatus(long driverId, DriverStatus status);
}
