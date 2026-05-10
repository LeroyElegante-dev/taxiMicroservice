package com.taxi.statisticsservice.service;

import com.taxi.common.dto.DailyTripStatsResponse;
import com.taxi.statisticsservice.repository.TripStatisticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StatisticsAppService {

    private final TripStatisticsRepository tripStatisticsRepository;

    public StatisticsAppService(TripStatisticsRepository tripStatisticsRepository) {
        this.tripStatisticsRepository = tripStatisticsRepository;
    }

    public DailyTripStatsResponse statsForDate(LocalDate date) {
        return tripStatisticsRepository.daily(date);
    }
}
