-- Hibernate маппит BigDecimal на NUMERIC; V2 создавал DOUBLE PRECISION — приводим к NUMERIC.
ALTER TABLE trips
    ALTER COLUMN origin_lat TYPE NUMERIC(12, 8) USING origin_lat::numeric,
    ALTER COLUMN origin_lng TYPE NUMERIC(12, 8) USING origin_lng::numeric,
    ALTER COLUMN dest_lat TYPE NUMERIC(12, 8) USING dest_lat::numeric,
    ALTER COLUMN dest_lng TYPE NUMERIC(12, 8) USING dest_lng::numeric;
