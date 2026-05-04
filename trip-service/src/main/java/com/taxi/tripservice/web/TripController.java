package com.taxi.tripservice.web;

import com.taxi.common.dto.TripCreateRequest;
import com.taxi.common.dto.TripRatingPatchRequest;
import com.taxi.common.dto.TripResponse;
import com.taxi.common.dto.TripStatusPatchRequest;
import com.taxi.tripservice.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/trips")
@Tag(name = "Trips", description = "Создание поездок, статусы, оценка")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать поездку", description = "Назначает свободного водителя, считает цену по Haversine × тариф")
    @ApiResponse(responseCode = "201", description = "Создано")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации", content = @Content)
    @ApiResponse(responseCode = "404", description = "Пассажир не найден", content = @Content)
    @ApiResponse(responseCode = "503", description = "Нет свободных водителей или User Service недоступен", content = @Content)
    public TripResponse create(@Valid @RequestBody TripCreateRequest request) {
        return tripService.createTrip(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Поездка по id")
    @ApiResponse(responseCode = "404", description = "Не найдено", content = @Content)
    public TripResponse get(@PathVariable long id) {
        return tripService.getById(id);
    }

    @GetMapping
    @Operation(summary = "История поездок пассажира")
    public List<TripResponse> list(@RequestParam("passenger_id") long passengerId) {
        return tripService.listForPassenger(passengerId);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Смена статуса поездки")
    @ApiResponse(responseCode = "409", description = "Недопустимый переход статуса", content = @Content)
    public TripResponse patchStatus(@PathVariable long id, @Valid @RequestBody TripStatusPatchRequest request) {
        return tripService.updateStatus(id, request);
    }

    @PatchMapping("/{id}/rating")
    @Operation(summary = "Оценка поездки (1–5) после завершения")
    @ApiResponse(responseCode = "409", description = "Поездка не завершена или оценка уже есть", content = @Content)
    public TripResponse patchRating(@PathVariable long id, @Valid @RequestBody TripRatingPatchRequest request) {
        return tripService.updateRating(id, request);
    }
}
