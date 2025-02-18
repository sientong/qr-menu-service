package com.qrmenu.integration;

import com.qrmenu.dto.auth.*;
import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import com.qrmenu.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationFlowIntegrationTest extends IntegrationTest {

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
    void shouldCompleteFullAuthenticationFlow() throws Exception {
        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(testUser.getEmail());
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        TokenResponse tokenResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                TokenResponse.class
        );

        // Refresh token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(tokenResponse.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        // Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken()))
                .andExpect(status().isOk());

        // Verify token is invalidated
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + tokenResponse.getAccessToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandlePasswordResetFlow() throws Exception {
        // Request password reset
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setEmail(testUser.getEmail());

        mockMvc.perform(post("/api/v1/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());

        // Confirm password reset
        PasswordResetConfirmRequest confirmRequest = new PasswordResetConfirmRequest();
        confirmRequest.setToken("invalid-token");
        confirmRequest.setNewPassword("newPassword123!");

        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleConcurrentLogins() throws Exception {
        // Login from multiple devices
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(testUser.getEmail());
        loginRequest.setPassword("password123");

        MvcResult[] loginResults = new MvcResult[3];
        for (int i = 0; i < 3; i++) {
            loginResults[i] = mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();
        }

        // Logout from all devices
        TokenResponse firstToken = objectMapper.readValue(
                loginResults[0].getResponse().getContentAsString(),
                TokenResponse.class
        );

        mockMvc.perform(post("/api/v1/auth/logout-all")
                .header("Authorization", "Bearer " + firstToken.getAccessToken()))
                .andExpect(status().isOk());

        // Verify all tokens are invalidated
        for (MvcResult result : loginResults) {
            TokenResponse token = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    TokenResponse.class
            );

            mockMvc.perform(post("/api/v1/auth/logout")
                    .header("Authorization", "Bearer " + token.getAccessToken()))
                    .andExpect(status().isUnauthorized());
        }
    }
} 