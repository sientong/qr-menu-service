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
public class AuthenticationMetricsAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.qrmenu.service.AuthenticationService.*(..))")
    public Object recordMethodMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        String methodName = joinPoint.getSignature().getName();
        
        try {
            Object result = joinPoint.proceed();
            sample.stop(Timer.builder("auth.method.timing")
                    .tag("method", methodName)
                    .tag("outcome", "success")
                    .register(meterRegistry));
            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder("auth.method.timing")
                    .tag("method", methodName)
                    .tag("outcome", "error")
                    .tag("error", e.getClass().getSimpleName())
                    .register(meterRegistry));
            throw e;
        }
    }
} 