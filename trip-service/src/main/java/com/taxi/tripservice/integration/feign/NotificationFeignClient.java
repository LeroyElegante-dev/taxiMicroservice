package com.taxi.tripservice.integration.feign;

import com.taxi.common.dto.NotificationTaskCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notificationServiceHttp",
        url = "${taxi.notifications.url}",
        configuration = FeignNotificationAuthConfig.class
)
public interface NotificationFeignClient {

    @PostMapping("/notifications")
    void enqueue(@RequestBody NotificationTaskCreateRequest body);
}
