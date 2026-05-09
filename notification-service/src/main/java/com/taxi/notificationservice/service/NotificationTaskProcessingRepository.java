package com.taxi.notificationservice.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationTaskProcessingRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationTaskProcessingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int markSent(long id) {
        return jdbcTemplate.update(
                """
                        UPDATE notification_tasks
                        SET status = 'SENT', updated_at = CURRENT_TIMESTAMP
                        WHERE id = ? AND status = 'PROCESSING'
                        """,
                id
        );
    }

    public int markFailure(long id) {
        return jdbcTemplate.update(
                """
                        UPDATE notification_tasks
                        SET retry_count = retry_count + 1,
                            status = CASE WHEN retry_count + 1 >= 3 THEN 'FAILED' ELSE 'PENDING' END,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ? AND status = 'PROCESSING'
                        """,
                id
        );
    }
}
