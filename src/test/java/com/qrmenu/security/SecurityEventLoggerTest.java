package com.qrmenu.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

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
            Range.from(Range.Bound.inclusive("-")).to(Range.Bound.inclusive("+"))
        );

        assertThat(streamEntries).isNotNull();
        assertThat(streamEntries).isNotEmpty();

        @SuppressWarnings("null")
        var lastEntry = streamEntries.get(streamEntries.size() - 1);
        Map<Object, Object> eventData = lastEntry.getValue();

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
                "TEST_EVENT_" + i,
                "test" + i + "@example.com",
                "Test event " + i
            );
        }

        var streamEntries = redisTemplate.opsForStream().range(
            "security:events",
            Range.from(Range.Bound.inclusive("-")).to(Range.Bound.inclusive("+"))
        );

        assertThat(streamEntries).hasSize(eventCount);
    }
}