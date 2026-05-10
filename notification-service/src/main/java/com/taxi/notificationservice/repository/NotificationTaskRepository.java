package com.taxi.notificationservice.repository;

import com.taxi.notificationservice.entity.NotificationTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTaskEntity, Long> {

    List<NotificationTaskEntity> findByTripIdOrderByCreatedAtAsc(Long tripId);
}
