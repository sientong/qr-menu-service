package com.qrmenu.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldEnforceRateLimit() throws Exception {
        String testPath = "/api/v1/test";
        
        // First 100 requests should succeed
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get(testPath)
                    .header("X-Forwarded-For", "192.168.1.1"))
                    .andExpect(status().isUnauthorized()); // Because we're not authenticated
        }

        // Next request should be rate limited
        mockMvc.perform(get(testPath)
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldTrackRateLimitsSeparatelyByIP() throws Exception {
        String testPath = "/api/v1/test";
        
        // Exhaust rate limit for first IP
        for (int i = 0; i < 101; i++) {
            mockMvc.perform(get(testPath)
                    .header("X-Forwarded-For", "192.168.1.1"));
        }

        // Different IP should still be allowed
        mockMvc.perform(get(testPath)
                .header("X-Forwarded-For", "192.168.1.2"))
                .andExpect(status().isUnauthorized()) // Not rate limited, just unauthorized
                .andReturn();
    }
} 