package io.abc.hrms.payroll.config;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect  // Marks this as an aspect - will be used to intercept method calls
@Component  // Makes this a Spring-managed bean
@RequiredArgsConstructor  // Lombok generates constructor for final fields
public class TracingConfig {
    
    // Automatically injected by Spring from OpenTelemetry autoconfiguration
    private final Tracer tracer;
    
    // Intercepts all methods in classes under io.abc.hrms.payroll.client
    @Around("execution(* io.abc.hrms.payroll.client.*.*(..))")
    public Object traceHttpCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        // Create span name from class and method: "DatacomApiClient.updateEmployee"
        String spanName = joinPoint.getSignature().getDeclaringType().getSimpleName() + 
                         "." + joinPoint.getSignature().getName();
        
        // Start a new trace span
        Span span = tracer.spanBuilder(spanName).startSpan();
        
        // Make the span the current active span and ensure it's closed properly
        try (var scope = span.makeCurrent()) {
            // Execute the original method
            return joinPoint.proceed();
        } catch (Exception e) {
            // If there's an error, record it in the span
            span.recordException(e);
            throw e;
        } finally {
            // Always end the span
            span.end();
        }
    }
} 