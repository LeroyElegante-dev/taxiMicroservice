package com.taxi.tripservice.integration.feign;

import com.taxi.common.dto.DriverResponse;
import com.taxi.common.dto.DriverStatusUpdateRequest;
import com.taxi.common.dto.PassengerResponse;
import com.taxi.common.model.DriverStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "userServiceHttp", url = "${taxi.user-service.url}")
public interface UserServiceFeignClient {

    @GetMapping("/passengers/{id}")
    PassengerResponse getPassenger(@PathVariable("id") long id);

    @GetMapping("/drivers")
    List<DriverResponse> listDrivers(@RequestParam("status") DriverStatus status);

    @PatchMapping(value = "/drivers/{id}/status", consumes = "application/json")
    DriverResponse updateDriverStatus(
            @PathVariable("id") long id,
            @RequestBody DriverStatusUpdateRequest request
    );
}
