package com.taxi.tripservice.config;

import feign.FeignException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfiguration {

    @Bean
    public Retry userServiceRetry() {
        IntervalFunction backoff = IntervalFunction.ofExponentialRandomBackoff(
                Duration.ofMillis(100),
                2.0,
                Duration.ofMillis(1000)
        );
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(4)
                .intervalFunction(backoff)
                .ignoreExceptions(FeignException.NotFound.class)
                .build();
        return Retry.of("userService", config);
    }
}
