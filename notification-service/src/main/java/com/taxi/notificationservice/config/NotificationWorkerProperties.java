package com.taxi.notificationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "taxi.notification.worker")
public class NotificationWorkerProperties {

    private int poolSize = 4;
    private long processingDelayMs = 400;
    private long idlePollMs = 150;
    /** Доля имитации ошибки отправки (0..1), для демонстрации повторов. */
    private double simulatedFailureRate = 0.0;

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public long getProcessingDelayMs() {
        return processingDelayMs;
    }

    public void setProcessingDelayMs(long processingDelayMs) {
        this.processingDelayMs = processingDelayMs;
    }

    public long getIdlePollMs() {
        return idlePollMs;
    }

    public void setIdlePollMs(long idlePollMs) {
        this.idlePollMs = idlePollMs;
    }

    public double getSimulatedFailureRate() {
        return simulatedFailureRate;
    }

    public void setSimulatedFailureRate(double simulatedFailureRate) {
        this.simulatedFailureRate = simulatedFailureRate;
    }
}
