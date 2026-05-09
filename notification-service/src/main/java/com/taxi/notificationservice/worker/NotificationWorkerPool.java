package com.taxi.notificationservice.worker;

import com.taxi.notificationservice.config.NotificationWorkerProperties;
import com.taxi.notificationservice.repository.NotificationTaskClaimRepository;
import com.taxi.notificationservice.service.NotificationTaskProcessingRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NotificationWorkerPool implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorkerPool.class);

    private final NotificationTaskClaimRepository claimRepository;
    private final NotificationTaskProcessingRepository processingRepository;
    private final NotificationWorkerProperties workerProperties;
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicInteger threadSeq = new AtomicInteger();
    private ExecutorService executor;

    public NotificationWorkerPool(
            NotificationTaskClaimRepository claimRepository,
            NotificationTaskProcessingRepository processingRepository,
            NotificationWorkerProperties workerProperties
    ) {
        this.claimRepository = claimRepository;
        this.processingRepository = processingRepository;
        this.workerProperties = workerProperties;
    }

    @PostConstruct
    void start() {
        int n = Math.min(Math.max(workerProperties.getPoolSize(), 3), 5);
        executor = Executors.newFixedThreadPool(n, r -> {
            Thread t = new Thread(r);
            t.setName("notification-worker-" + threadSeq.incrementAndGet());
            t.setDaemon(false);
            return t;
        });
        for (int i = 0; i < n; i++) {
            executor.submit(this::workerLoop);
        }
        log.info("Запущен пул уведомлений: {} потоков", n);
    }

    private void workerLoop() {
        while (!stopRequested.get() && !Thread.currentThread().isInterrupted()) {
            try {
                Optional<Long> taskId = claimRepository.claimNextPending();
                if (taskId.isEmpty()) {
                    Thread.sleep(workerProperties.getIdlePollMs());
                    continue;
                }
                processTask(taskId.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Ошибка воркера уведомлений", e);
            }
        }
    }

    private void processTask(long id) throws InterruptedException {
        Thread.sleep(workerProperties.getProcessingDelayMs());
        double rate = workerProperties.getSimulatedFailureRate();
        if (rate > 0 && ThreadLocalRandom.current().nextDouble() < rate) {
            log.warn("Имитация сбоя отправки notification task id={}", id);
            processingRepository.markFailure(id);
            return;
        }
        log.info("Уведомление отправлено (имитация), task id={}, задержка {} ms", id, workerProperties.getProcessingDelayMs());
        int updated = processingRepository.markSent(id);
        if (updated == 0) {
            log.debug("markSent не затронул строк id={}, возможно гонка", id);
        }
    }

    @Override
    public void destroy() throws Exception {
        stopRequested.set(true);
        if (executor == null) {
            return;
        }
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            log.warn("Воркеры не завершились за 60 с, принудительная остановка");
            executor.shutdownNow();
            executor.awaitTermination(15, TimeUnit.SECONDS);
        } else {
            log.info("Пул уведомлений корректно остановлен");
        }
    }
}
