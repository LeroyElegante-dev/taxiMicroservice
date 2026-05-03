package com.taxi.userservice.web;

import com.taxi.common.dto.DriverRegistrationRequest;
import com.taxi.common.dto.DriverResponse;
import com.taxi.common.dto.DriverStatusUpdateRequest;
import com.taxi.common.model.DriverStatus;
import com.taxi.userservice.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/drivers")
@Tag(name = "Drivers", description = "Регистрация, профили и статусы водителей")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация водителя", description = "Поле status необязательно; по умолчанию FREE")
    @ApiResponse(responseCode = "201", description = "Создано")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email уже занят", content = @Content)
    public DriverResponse register(@Valid @RequestBody DriverRegistrationRequest request) {
        return driverService.register(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Профиль водителя по id")
    @ApiResponse(responseCode = "200", description = "Найдено")
    @ApiResponse(responseCode = "404", description = "Не найдено", content = @Content)
    public DriverResponse get(@PathVariable Long id) {
        return driverService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Список водителей", description = "При указании status возвращаются только водители с этим статусом (например FREE)")
    @ApiResponse(responseCode = "200", description = "Список")
    @ApiResponse(responseCode = "400", description = "Некорректный статус в query", content = @Content)
    public List<DriverResponse> list(
            @Parameter(description = "Фильтр по статусу: FREE, BUSY, OFFLINE")
            @RequestParam(required = false) DriverStatus status
    ) {
        return driverService.list(status);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Обновление статуса водителя")
    @ApiResponse(responseCode = "200", description = "Обновлено")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
    @ApiResponse(responseCode = "404", description = "Водитель не найден", content = @Content)
    public DriverResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody DriverStatusUpdateRequest request
    ) {
        return driverService.updateStatus(id, request);
    }
}
