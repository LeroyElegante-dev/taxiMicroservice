package com.taxi.statisticsservice.repository;

import com.taxi.common.dto.DailyTripStatsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

@Repository
public class TripStatisticsRepository {

    private static final String SUMMARY_SQL = """
            SELECT COUNT(*) AS cnt,
                   COALESCE(AVG(price), 0) AS avg_price
            FROM trips
            WHERE (created_at AT TIME ZONE 'UTC')::date = ?::date
            """;

    private static final String ROUTES_SQL = """
            SELECT origin_lat, origin_lng, dest_lat, dest_lng, COUNT(*) AS trip_count
            FROM trips
            WHERE (created_at AT TIME ZONE 'UTC')::date = ?::date
            GROUP BY origin_lat, origin_lng, dest_lat, dest_lng
            ORDER BY trip_count DESC
            LIMIT 5
            """;

    private final JdbcTemplate jdbcTemplate;

    public TripStatisticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DailyTripStatsResponse daily(LocalDate date) {
        var summary = jdbcTemplate.queryForObject(
                SUMMARY_SQL,
                (rs, rowNum) -> new Summary(rs.getLong("cnt"), rs.getBigDecimal("avg_price")),
                date
        );
        List<DailyTripStatsResponse.PopularRouteStat> routes = jdbcTemplate.query(
                ROUTES_SQL,
                popularMapper(),
                date
        );
        return new DailyTripStatsResponse(
                date.toString(),
                summary.count(),
                summary.avgPrice(),
                routes
        );
    }

    private RowMapper<DailyTripStatsResponse.PopularRouteStat> popularMapper() {
        return (ResultSet rs, int rowNum) -> new DailyTripStatsResponse.PopularRouteStat(
                rs.getBigDecimal("origin_lat"),
                rs.getBigDecimal("origin_lng"),
                rs.getBigDecimal("dest_lat"),
                rs.getBigDecimal("dest_lng"),
                rs.getLong("trip_count")
        );
    }

    private record Summary(long count, BigDecimal avgPrice) {
    }
}
