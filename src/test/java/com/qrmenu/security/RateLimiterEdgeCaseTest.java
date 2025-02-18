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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RateLimiterEdgeCaseTest {

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void shouldHandleConcurrentRequests() throws InterruptedException {
        int threadCount = 10;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        String testKey = "concurrent-test";

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        if (rateLimiter.tryAcquire(testKey)) {
                            successCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        assertThat(successCount.get()).isLessThanOrEqualTo(100); // Max requests allowed
    }

    @Test
    void shouldHandleRedisFailure() {
        // Simulate Redis failure by using invalid connection
        redisTemplate.getConnectionFactory().getConnection().close();
        
        String testKey = "failure-test";
        assertThat(rateLimiter.tryAcquire(testKey)).isFalse();
    }

    @Test
    void shouldHandleLongKeys() {
        StringBuilder longKey = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longKey.append("very-long-key-");
        }

        assertThat(rateLimiter.tryAcquire(longKey.toString())).isTrue();
    }

    @Test
    void shouldHandleSpecialCharacters() {
        String[] specialKeys = {
            "key!@#$%^&*()",
            "key\n\t\r",
            "key\u0000",
            "key\u2022"
        };

        for (String key : specialKeys) {
            assertThat(rateLimiter.tryAcquire(key)).isTrue();
        }
    }

    @Test
    void shouldHandleWindowBoundary() throws InterruptedException {
        String testKey = "window-test";
        
        // Fill up the current window
        for (int i = 0; i < 100; i++) {
            rateLimiter.tryAcquire(testKey);
        }
        
        // Wait for window to expire
        Thread.sleep(60_000);
        
        // Should be allowed again
        assertThat(rateLimiter.tryAcquire(testKey)).isTrue();
    }
} 