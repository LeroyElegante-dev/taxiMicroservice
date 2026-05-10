package com.taxi.notificationservice.web;

import com.taxi.common.dto.NotificationTaskCreateRequest;
import com.taxi.common.dto.NotificationTaskResponse;
import com.taxi.notificationservice.service.NotificationAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "Очередь уведомлений")
public class NotificationController {

    private final NotificationAppService notificationAppService;

    public NotificationController(NotificationAppService notificationAppService) {
        this.notificationAppService = notificationAppService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Поставить задачу в очередь")
    public NotificationTaskResponse create(@Valid @RequestBody NotificationTaskCreateRequest request) {
        return notificationAppService.enqueue(request);
    }

    @GetMapping
    @Operation(summary = "Задачи по поездке")
    public List<NotificationTaskResponse> list(@RequestParam("trip_id") long tripId) {
        return notificationAppService.listByTrip(tripId);
    }
}
