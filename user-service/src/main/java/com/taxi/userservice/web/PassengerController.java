package com.taxi.userservice.web;

import com.taxi.common.dto.PassengerRegistrationRequest;
import com.taxi.common.dto.PassengerResponse;
import com.taxi.userservice.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/passengers")
@Tag(name = "Passengers", description = "Регистрация и профили пассажиров")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Регистрация пассажира")
    @ApiResponse(responseCode = "201", description = "Создано")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email уже занят", content = @Content)
    public PassengerResponse register(@Valid @RequestBody PassengerRegistrationRequest request) {
        return passengerService.register(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Профиль пассажира по id")
    @ApiResponse(responseCode = "200", description = "Найдено")
    @ApiResponse(responseCode = "404", description = "Не найдено", content = @Content)
    public PassengerResponse get(@PathVariable Long id) {
        return passengerService.getById(id);
    }
}
