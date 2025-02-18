package com.qrmenu.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthServiceHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Health health() {
        try {
            redisTemplate.opsForValue().get("health-check");
            return Health.up()
                    .withDetail("redis", "Connected")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("redis", "Disconnected")
                    .withException(e)
                    .build();
        }
    }
} 