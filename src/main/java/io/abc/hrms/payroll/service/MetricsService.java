package io.abc.hrms.payroll.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final MeterRegistry registry;
    
    public void recordEvent(String eventType, boolean success) {
        Counter.builder("hrms.events")
            .tag("type", eventType)
            .tag("status", success ? "success" : "failure")
            .description("Number of events processed")
            .register(registry)
            .increment();
    }
} 