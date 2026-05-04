package com.taxi.tripservice.integration;

import com.taxi.common.model.TripStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingNotificationClient implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationClient.class);

    @Override
    public void onTripStatusChanged(long tripId, TripStatus newStatus, String details) {
        log.info("Notification stub: tripId={}, status={}, {}", tripId, newStatus, details);
    }
}
