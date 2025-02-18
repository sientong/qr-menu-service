package com.qrmenu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.dto.auth.LoginRequest;
import com.qrmenu.dto.auth.RefreshTokenRequest;
import com.qrmenu.dto.auth.TokenResponse;
import com.qrmenu.exception.AuthenticationException;
import com.qrmenu.model.User;
import com.qrmenu.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authService;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .expiresIn(3600)
                .tokenType("Bearer")
                .build();

        when(authService.login(request.getEmail(), request.getPassword()))
                .thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(tokenResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(tokenResponse.getRefreshToken()));
    }

    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .expiresIn(3600)
                .tokenType("Bearer")
                .build();

        when(authService.refreshToken(request.getRefreshToken()))
                .thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(tokenResponse.getAccessToken()))
                .andExpect(jsonPath("$.refreshToken").value(tokenResponse.getRefreshToken()));
    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        // Given
        String token = "valid-token";
        
        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        verify(authService).logout(token);
    }
} 