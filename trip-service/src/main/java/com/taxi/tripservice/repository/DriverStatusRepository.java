package com.taxi.tripservice.repository;

import com.taxi.common.model.DriverStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DriverStatusRepository {

    private final JdbcTemplate jdbcTemplate;

    public DriverStatusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setStatus(long driverId, DriverStatus status) {
        jdbcTemplate.update(
                "UPDATE drivers SET status = ? WHERE id = ?",
                status.name(),
                driverId
        );
    }
}
