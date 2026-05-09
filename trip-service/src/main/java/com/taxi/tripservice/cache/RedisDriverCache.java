package com.taxi.tripservice.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxi.common.dto.DriverResponse;
import com.taxi.tripservice.config.TaxiRedisProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "taxi.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisDriverCache implements DriverCache {

    private static final Logger log = LoggerFactory.getLogger(RedisDriverCache.class);
    private static final String KEY = "taxi:drivers:free";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final TaxiRedisProperties redisProperties;

    public RedisDriverCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            TaxiRedisProperties redisProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.redisProperties = redisProperties;
    }

    @Override
    public Optional<List<DriverResponse>> getFreeDrivers() {
        try {
            String json = redisTemplate.opsForValue().get(KEY);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            List<DriverResponse> list = objectMapper.readValue(json, new TypeReference<List<DriverResponse>>() {});
            log.info("В Redis найден кэш списка FREE-водителей ({} записей)", list.size());
            return Optional.of(list);
        } catch (Exception e) {
            log.warn("Не удалось прочитать кэш водителей из Redis", e);
            return Optional.empty();
        }
    }

    @Override
    public void putFreeDrivers(List<DriverResponse> drivers) {
        try {
            String json = objectMapper.writeValueAsString(drivers);
            long ttl = redisProperties.getTtlSeconds();
            redisTemplate.opsForValue().set(KEY, json, Duration.ofSeconds(ttl));
            log.info("В Redis записан список из {} FREE-водителей (TTL {} с)", drivers.size(), ttl);
        } catch (Exception e) {
            log.warn("Не удалось записать кэш водителей в Redis", e);
        }
    }

    @Override
    public void invalidateFreeDrivers() {
        redisTemplate.delete(KEY);
        log.info("Кэш FREE-водителей в Redis сброшен");
    }
}
