ALTER TABLE trips
    ADD COLUMN IF NOT EXISTS client_request_id UUID;

CREATE UNIQUE INDEX IF NOT EXISTS uk_trips_client_request_id
    ON trips (client_request_id)
    WHERE client_request_id IS NOT NULL;
