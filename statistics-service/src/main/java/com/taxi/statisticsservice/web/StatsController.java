package com.taxi.statisticsservice.web;

import com.taxi.common.dto.DailyTripStatsResponse;
import com.taxi.statisticsservice.service.StatisticsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/stats")
@Tag(name = "Statistics", description = "Сводки по поездкам")
public class StatsController {

    private final StatisticsAppService statisticsAppService;

    public StatsController(StatisticsAppService statisticsAppService) {
        this.statisticsAppService = statisticsAppService;
    }

    @GetMapping
    @Operation(summary = "Статистика за календарный день (UTC)")
    public DailyTripStatsResponse stats(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return statisticsAppService.statsForDate(date);
    }
}
