package com.qrmenu.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class SecurityEventLoggerEdgeCaseTest {

    @Autowired
    private SecurityEventLogger securityEventLogger;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void shouldHandleConcurrentEventLogging() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    securityEventLogger.logEvent(
                        "CONCURRENT_TEST",
                        "user" + index + "@example.com",
                        "Concurrent test event"
                    );
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        
        var streamEntries = redisTemplate.opsForStream().range(
            "security:events",
            Range.unbounded()
        );
        assertThat(streamEntries).hasSizeGreaterThanOrEqualTo(threadCount);
    }

    @Test
    void shouldHandleLargeEventDetails() {
        StringBuilder largeDetails = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeDetails.append("Large event details ");
        }

        assertDoesNotThrow(() ->
            securityEventLogger.logEvent(
                "LARGE_EVENT",
                "test@example.com",
                largeDetails.toString()
            )
        );
    }

    @Test
    void shouldHandleSpecialCharactersInEvents() {
        String[] specialStrings = {
            "Event\nwith\nnewlines",
            "Event\twith\ttabs",
            "Event with unicode: \u2022\u2023\u2024",
            "Event with emoji: 🔒🔑📝"
        };

        for (String str : specialStrings) {
            assertDoesNotThrow(() ->
                securityEventLogger.logEvent(
                    "SPECIAL_CHARS",
                    "test@example.com",
                    str
                )
            );
        }
    }
} 