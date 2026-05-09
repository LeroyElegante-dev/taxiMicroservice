package com.taxi.tripservice.integration;

import com.taxi.common.dto.NotificationTaskCreateRequest;
import com.taxi.common.dto.RecipientType;
import com.taxi.common.model.TripStatus;
import com.taxi.tripservice.integration.feign.NotificationFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class FeignNotificationClient implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(FeignNotificationClient.class);

    private final NotificationFeignClient notificationFeignClient;

    public FeignNotificationClient(NotificationFeignClient notificationFeignClient) {
        this.notificationFeignClient = notificationFeignClient;
    }

    @Override
    public void onTripStatusChanged(long tripId, TripStatus newStatus, String details) {
        try {
            String message = details + " [" + newStatus + "]";
            notificationFeignClient.enqueue(new NotificationTaskCreateRequest(tripId, RecipientType.BOTH, message));
        } catch (Exception e) {
            log.warn("Notification Service недоступен, событие поездки {} не поставлено в очередь: {}", tripId, e.getMessage());
        }
    }
}
