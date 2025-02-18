package com.qrmenu.integration;

import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityHeadersIntegrationTest extends IntegrationTest {

    @Test
    void shouldIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/auth/test"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Referrer-Policy"))
                .andExpect(header().exists("Permissions-Policy"))
                .andExpect(header().exists("Feature-Policy"));
    }

    @Test
    void shouldEnforceCorsPolicy() throws Exception {
        mockMvc.perform(get("/api/v1/auth/test")
                .header("Origin", "https://malicious-site.com"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/auth/test")
                .header("Origin", "https://your-frontend-domain.com"))
                .andExpect(status().isOk());
    }
} 