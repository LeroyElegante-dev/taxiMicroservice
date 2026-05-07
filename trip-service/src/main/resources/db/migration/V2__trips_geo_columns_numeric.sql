-- Hibernate маппит BigDecimal на NUMERIC; V1 мог создать DOUBLE PRECISION — приводим к NUMERIC, если нужно.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'trips'
          AND column_name = 'origin_lat'
          AND data_type = 'double precision'
    ) THEN
        ALTER TABLE trips
            ALTER COLUMN origin_lat TYPE NUMERIC(12, 8) USING origin_lat::numeric,
            ALTER COLUMN origin_lng TYPE NUMERIC(12, 8) USING origin_lng::numeric,
            ALTER COLUMN dest_lat TYPE NUMERIC(12, 8) USING dest_lat::numeric,
            ALTER COLUMN dest_lng TYPE NUMERIC(12, 8) USING dest_lng::numeric;
    END IF;
END $$;
