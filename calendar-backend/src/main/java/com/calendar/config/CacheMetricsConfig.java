package com.calendar.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for cache metrics tracking.
 * Monitors Redis cache hit/miss ratios and performance.
 * 
 * Note: CacheManager is defined in RedisConfig.
 * This class is reserved for future cache-specific metrics configuration.
 */
@Configuration
@EnableCaching
public class CacheMetricsConfig {

    private final MeterRegistry meterRegistry;

    public CacheMetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // Cache metrics will be automatically collected by Spring Boot Actuator
    // when cache statistics are enabled in application.properties
}
