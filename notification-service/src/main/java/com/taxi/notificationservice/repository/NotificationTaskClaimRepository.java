package com.taxi.notificationservice.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NotificationTaskClaimRepository {

    private static final String CLAIM_SQL = """
            WITH picked AS (
                SELECT id FROM notification_tasks
                WHERE status = 'PENDING' AND retry_count < 3
                ORDER BY id
                FOR UPDATE SKIP LOCKED
                FETCH FIRST 1 ROW ONLY
            )
            UPDATE notification_tasks n
            SET status = 'PROCESSING', updated_at = CURRENT_TIMESTAMP
            FROM picked
            WHERE n.id = picked.id
            RETURNING n.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public NotificationTaskClaimRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Long> claimNextPending() {
        List<Long> ids = jdbcTemplate.query(CLAIM_SQL, (rs, rowNum) -> rs.getLong(1));
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ids.get(0));
    }
}
