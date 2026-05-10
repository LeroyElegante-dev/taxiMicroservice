package com.taxi.notificationservice.service;

import com.taxi.common.dto.NotificationTaskCreateRequest;
import com.taxi.common.dto.NotificationTaskResponse;
import com.taxi.notificationservice.entity.NotificationTaskEntity;
import com.taxi.notificationservice.model.NotificationTaskStatus;
import com.taxi.notificationservice.repository.NotificationTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationAppService {

    private final NotificationTaskRepository taskRepository;

    public NotificationAppService(NotificationTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public NotificationTaskResponse enqueue(NotificationTaskCreateRequest request) {
        NotificationTaskEntity e = new NotificationTaskEntity();
        e.setTripId(request.tripId());
        e.setRecipientType(request.recipientType());
        e.setMessage(request.message());
        e.setStatus(NotificationTaskStatus.PENDING);
        e.setRetryCount(0);
        NotificationTaskEntity saved = taskRepository.save(e);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationTaskResponse> listByTrip(long tripId) {
        return taskRepository.findByTripIdOrderByCreatedAtAsc(tripId).stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationTaskResponse toResponse(NotificationTaskEntity e) {
        return new NotificationTaskResponse(
                e.getId(),
                e.getTripId(),
                e.getRecipientType(),
                e.getMessage(),
                e.getStatus().name(),
                e.getRetryCount(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
