package com.qrmenu.integration;

import com.qrmenu.dto.auth.LoginRequest;
import com.qrmenu.dto.auth.PasswordResetRequest;
import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import com.qrmenu.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationIntegrationTest extends IntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(UserRole.RESTAURANT_OWNER)
                .build();
        testUser = userService.createUser(testUser);
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(testUser.getEmail());
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(testUser.getEmail());
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void shouldInitiatePasswordReset() throws Exception {
        // Given
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail(testUser.getEmail());

        // When & Then
        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldHandleRateLimitExceeded() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail(testUser.getEmail());
        request.setPassword("wrongpassword");

        // When & Then
        for (int i = 0; i < 6; i++) { // Exceeding the rate limit (5 attempts per hour)
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(i < 5 ? 
                            status().isUnauthorized() : 
                            status().isTooManyRequests());
        }
    }
} 