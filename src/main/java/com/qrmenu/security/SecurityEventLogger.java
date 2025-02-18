package com.qrmenu.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SecurityEventLogger {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String EVENT_PREFIX = "security:event:";
    private static final String EVENT_STREAM = "security:events";

    public void logEvent(String eventType, String username, String details) {
        String eventId = EVENT_PREFIX + System.currentTimeMillis();
        Map<String, String> event = Map.of(
            "eventType", eventType,
            "username", username,
            "details", details,
            "timestamp", LocalDateTime.now().toString()
        );
        
        redisTemplate.opsForStream().add(EVENT_STREAM, event);
        redisTemplate.opsForHash().putAll(eventId, event);
    }
} 