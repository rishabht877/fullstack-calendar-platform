package com.calendar.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PerformanceAspectTest {

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private PerformanceAspect performanceAspect;

    @BeforeEach
    public void setUp() {
        // Inject real registry manually since InjectMocks might not handle it if not a mock/spy
        performanceAspect = new PerformanceAspect(meterRegistry);
        
        // Basic stubs used in multiple tests
        lenient().when(joinPoint.getSignature()).thenReturn(signature);
    }

    @Test
    public void measureServiceMethodPerformance_Success_RecordsMetric() throws Throwable {
        when(signature.getDeclaringType()).thenReturn(com.calendar.service.EventService.class);
        when(signature.getName()).thenReturn("getEvents");
        when(joinPoint.proceed()).thenReturn("OK");

        Object result = performanceAspect.measureServiceMethodPerformance(joinPoint);

        assertEquals("OK", result);
        
        Timer timer = meterRegistry.find("service.event.getEvents").timer();
        assertEquals(1, timer.count());
    }

    @Test
    public void measureServiceMethodPerformance_Exception_RecordsErrorMetric() throws Throwable {
        when(signature.getDeclaringType()).thenReturn(com.calendar.service.EventService.class);
        when(signature.getName()).thenReturn("createEvent");
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Service Error"));

        assertThrows(RuntimeException.class, () -> {
            performanceAspect.measureServiceMethodPerformance(joinPoint);
        });

        Timer timer = meterRegistry.find("service.event.createEvent.error").timer();
        assertEquals(1, timer.count());
    }

    @Test
    public void measureAnalyticsPerformance_Success_RecordsAnalyticsMetric() throws Throwable {
        when(joinPoint.proceed()).thenReturn("Analytics Data");

        Object result = performanceAspect.measureAnalyticsPerformance(joinPoint);

        assertEquals("Analytics Data", result);
        
        Timer timer = meterRegistry.find("analytics.query.time").timer();
        assertEquals(1, timer.count());
    }
    
    @Test
    public void measureAnalyticsPerformance_Exception_RecordsErrorMetric() throws Throwable {
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Analytics Error"));

        assertThrows(RuntimeException.class, () -> {
            performanceAspect.measureAnalyticsPerformance(joinPoint);
        });

        Timer timer = meterRegistry.find("analytics.query.error").timer();
        assertEquals(1, timer.count());
    }
}
