package com.taxi.tripservice.integration;

import com.taxi.common.dto.DriverResponse;
import com.taxi.common.dto.DriverStatusUpdateRequest;
import com.taxi.common.dto.PassengerResponse;
import com.taxi.common.model.DriverStatus;
import com.taxi.tripservice.integration.feign.UserServiceFeignClient;
import com.taxi.tripservice.support.UpstreamUnavailableException;
import feign.FeignException;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Primary
public class ResilientDriverServiceClient implements DriverServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientDriverServiceClient.class);

    private final UserServiceFeignClient feign;
    private final Retry userServiceRetry;

    public ResilientDriverServiceClient(UserServiceFeignClient feign, Retry userServiceRetry) {
        this.feign = feign;
        this.userServiceRetry = userServiceRetry;
    }

    @Override
    public PassengerResponse getPassenger(long passengerId) {
        try {
            return Retry.decorateSupplier(userServiceRetry, () -> feign.getPassenger(passengerId)).get();
        } catch (FeignException.NotFound e) {
            throw e;
        } catch (Exception e) {
            throw new UpstreamUnavailableException(
                    "User Service недоступен: не удалось получить пассажира id=" + passengerId,
                    e
            );
        }
    }

    @Override
    public List<DriverResponse> listFreeDrivers() {
        try {
            return Retry.decorateSupplier(userServiceRetry, () -> feign.listDrivers(DriverStatus.FREE)).get();
        } catch (Exception e) {
            log.warn("User Service недоступен: fallback для списка FREE-водителей — пустой список", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void updateDriverStatus(long driverId, DriverStatus status) {
        try {
            Retry.decorateRunnable(
                    userServiceRetry,
                    () -> feign.updateDriverStatus(driverId, new DriverStatusUpdateRequest(status))
            ).run();
        } catch (Exception e) {
            log.warn("User Service недоступен: не удалось PATCH статуса водителя id={} на {}", driverId, status, e);
        }
    }
}
