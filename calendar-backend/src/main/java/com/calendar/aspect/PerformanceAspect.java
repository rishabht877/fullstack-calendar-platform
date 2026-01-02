package com.calendar.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect for measuring performance of service methods.
 * Tracks execution time and logs slow queries.
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);
    private static final long SLOW_QUERY_THRESHOLD_MS = 100;

    private final MeterRegistry meterRegistry;

    public PerformanceAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Measure execution time of all service methods.
     */
    @Around("execution(* com.calendar.service.*.*(..))")
    public Object measureServiceMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String metricName = "service." + className.toLowerCase().replace("service", "") + "." + methodName;

        Timer.Sample sample = Timer.start(meterRegistry);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // Record metric
            sample.stop(Timer.builder(metricName)
                    .description("Execution time for " + className + "." + methodName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry));

            // Log slow queries
            if (executionTime > SLOW_QUERY_THRESHOLD_MS) {
                logger.warn("Slow query detected: {}.{} took {}ms", 
                    className, methodName, executionTime);
            } else {
                logger.debug("{}.{} executed in {}ms", 
                    className, methodName, executionTime);
            }

            return result;
        } catch (Throwable throwable) {
            sample.stop(Timer.builder(metricName + ".error")
                    .description("Error execution time for " + className + "." + methodName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry));
            throw throwable;
        }
    }

    /**
     * Specifically track analytics query performance.
     */
    @Around("execution(* com.calendar.service.AnalyticsService.getUserAnalytics(..))")
    public Object measureAnalyticsPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // Record detailed analytics metric
            sample.stop(Timer.builder("analytics.query.time")
                    .description("Analytics query execution time")
                    .tag("cached", "unknown")
                    .register(meterRegistry));

            logger.info("Analytics query executed in {}ms", executionTime);

            return result;
        } catch (Throwable throwable) {
            sample.stop(Timer.builder("analytics.query.error")
                    .description("Analytics query error time")
                    .register(meterRegistry));
            throw throwable;
        }
    }
}
