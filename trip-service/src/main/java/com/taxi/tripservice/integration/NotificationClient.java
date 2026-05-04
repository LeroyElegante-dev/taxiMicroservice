package com.taxi.tripservice.integration;

import com.taxi.common.model.TripStatus;

/**
 * Уведомления о событиях поездки. Третий участник подключит Notification Service.
 */
public interface NotificationClient {

    void onTripStatusChanged(long tripId, TripStatus newStatus, String details);
}
