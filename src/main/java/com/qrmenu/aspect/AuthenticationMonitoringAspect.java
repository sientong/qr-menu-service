package com.qrmenu.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthenticationMonitoringAspect {

    private final MeterRegistry meterRegistry;

    @Around("execution(* com.qrmenu.service.AuthenticationService.login(..))")
    public Object monitorLoginAttempts(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            meterRegistry.counter("auth.login.success").increment();
            return result;
        } catch (Exception e) {
            meterRegistry.counter("auth.login.failure", 
                "reason", e.getClass().getSimpleName()).increment();
            throw e;
        }
    }

    @Around("execution(* com.qrmenu.service.AuthenticationService.refreshToken(..))")
    public Object monitorTokenRefresh(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            meterRegistry.counter("auth.token.refresh.success").increment();
            return result;
        } catch (Exception e) {
            meterRegistry.counter("auth.token.refresh.failure",
                "reason", e.getClass().getSimpleName()).increment();
            throw e;
        }
    }
} 