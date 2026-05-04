package com.taxi.tripservice.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DriverClaimRepository {

    private static final String CLAIM_SQL = """
            WITH picked AS (
                SELECT id FROM drivers
                WHERE status = 'FREE'
                ORDER BY id
                FOR UPDATE SKIP LOCKED
                FETCH FIRST 1 ROW ONLY
            )
            UPDATE drivers d
            SET status = 'BUSY'
            FROM picked
            WHERE d.id = picked.id
            RETURNING d.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public DriverClaimRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Long> claimNextFreeDriver() {
        List<Long> ids = jdbcTemplate.query(CLAIM_SQL, (rs, rowNum) -> rs.getLong(1));
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ids.get(0));
    }
}
