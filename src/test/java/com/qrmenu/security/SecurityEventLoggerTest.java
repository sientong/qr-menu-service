package com.qrmenu.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SecurityEventLoggerTest {

    @Autowired
    private SecurityEventLogger securityEventLogger;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void shouldLogSecurityEvent() {
        String username = "test@example.com";
        String eventType = "TEST_EVENT";
        String details = "Test security event";

        securityEventLogger.logEvent(eventType, username, details);

        // Verify event in stream
        var streamEntries = redisTemplate.opsForStream().range(
            "security:events",
            Range.unbounded()
        );
        
        assertThat(streamEntries).isNotEmpty();
        var lastEntry = streamEntries.get(streamEntries.size() - 1);
        Map<String, String> eventData = lastEntry.getValue();
        
        assertThat(eventData)
            .containsEntry("eventType", eventType)
            .containsEntry("username", username)
            .containsEntry("details", details)
            .containsKey("timestamp");
    }

    @Test
    void shouldHandleMultipleEvents() {
        int eventCount = 5;
        for (int i = 0; i < eventCount; i++) {
            securityEventLogger.logEvent(
                "TEST_EVENT",
                "user" + i + "@example.com",
                "Test event " + i
            );
        }

        var streamEntries = redisTemplate.opsForStream().range(
            "security:events",
            Range.unbounded()
        );
        
        assertThat(streamEntries).hasSizeGreaterThanOrEqualTo(eventCount);
    }
} 