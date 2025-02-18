package com.qrmenu.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class AlertConfig {

    private final MeterRegistry meterRegistry;
    private final HealthEndpoint healthEndpoint;

    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkHealthMetrics() {
        Gauge.builder("health.status", healthEndpoint,
                this::getHealthScore)
                .description("Application health status")
                .tag("application", "qr-menu-service")
                .register(meterRegistry);
    }

    private int getHealthScore(HealthEndpoint health) {
        return health.health().getStatus() == Status.UP ? 1 : 0;
    }
} 