package com.qrmenu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.qrmenu.config.TokenConfig;
import com.qrmenu.dto.auth.TokenResponse;
import com.qrmenu.exception.AuthenticationException;
import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import com.qrmenu.service.impl.AuthenticationServiceImpl;

import io.micrometer.core.instrument.MeterRegistry;


@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private TokenConfig tokenConfig;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private EmailService emailService;

    @Mock
    MeterRegistry meterRegistry;

    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        authService = new AuthenticationServiceImpl(userService, passwordEncoder, redisTemplate, tokenConfig, rateLimitService, emailService, meterRegistry);
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        User user = User.builder()
                .id(1L)
                .email(email)
                .passwordHash("hashedPassword")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        when(userService.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);

        // When
        TokenResponse token = authService.login(email, password);

        // Then
        assertThat(token.getAccessToken()).isNotNull();
        verify(valueOperations).set(
                eq("token:" + token),
                eq(user.getId().toString()),
                any(Duration.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenLoginWithInvalidCredentials() {
        // Given
        String email = "test@example.com";
        String password = "wrongPassword";
        User user = User.builder()
                .email(email)
                .passwordHash("hashedPassword")
                .build();

        when(userService.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(email, password))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        String token = "valid-token";
        String userId = "1";
        when(valueOperations.get("token:" + token)).thenReturn(userId);

        // When
        boolean isValid = authService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldInvalidateTokenOnLogout() {
        // Given
        String token = "valid-token";

        // When
        authService.logout(token);

        // Then
        verify(redisTemplate).delete("token:" + token);
    }

    @Test
    void shouldGetCurrentUser() {
        // Given
        String token = "valid-token";
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        when(valueOperations.get("token:" + token)).thenReturn(userId.toString());
        when(userService.findById(userId)).thenReturn(Optional.of(user));

        // When
        User currentUser = authService.getCurrentUser(token);

        // Then
        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getId()).isEqualTo(userId);
    }
}