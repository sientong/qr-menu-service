package com.qrmenu.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class StockManagementMetricsAspect {
    private final MeterRegistry registry;

    @Around("execution(* com.qrmenu.service.StockManagementService.*(..))")
    public Object measureStockOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(registry);
        try {
            return joinPoint.proceed();
        } finally {
            sample.stop(Timer.builder("stock.operations")
                    .tag("operation", joinPoint.getSignature().getName())
                    .description("Stock management operation timing")
                    .register(registry));
        }
    }
} 