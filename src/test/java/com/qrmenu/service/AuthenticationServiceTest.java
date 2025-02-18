package com.qrmenu.service;

import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import com.qrmenu.service.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        authenticationService = new AuthenticationServiceImpl(userService, passwordEncoder, redisTemplate);
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        String email = "test@example.com";
        String password = "password";
        User user = User.builder()
                .id(1L)
                .email(email)
                .passwordHash("hashedPassword")
                .role(UserRole.RESTAURANT_OWNER)
                .build();

        when(userService.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPasswordHash())).thenReturn(true);
        when(valueOperations.get(any())).thenReturn(null);

        // When
        String token = authenticationService.login(email, password);

        // Then
        assertThat(token).isNotNull();
        verify(valueOperations).set(eq("token:" + token), eq(user.getId().toString()), any());
    }

    @Test
    void shouldValidateToken() {
        // Given
        String token = UUID.randomUUID().toString();
        when(valueOperations.get("token:" + token)).thenReturn("1");

        // When
        boolean isValid = authenticationService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }
} 