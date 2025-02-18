package com.qrmenu.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RateLimiterTest {

    @Autowired
    private RateLimiter rateLimiter;

    @Test
    void shouldLimitRequests() {
        String testKey = "test:key";
        
        // First 100 requests should succeed
        for (int i = 0; i < 100; i++) {
            assertThat(rateLimiter.tryAcquire(testKey)).isTrue();
        }
        
        // Next request should fail
        assertThat(rateLimiter.tryAcquire(testKey)).isFalse();
    }
} 