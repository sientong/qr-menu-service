package com.qrmenu.service.impl;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.qrmenu.config.TokenConfig;
import com.qrmenu.dto.auth.TokenResponse;
import com.qrmenu.exception.AuthenticationException;
import com.qrmenu.model.User;
import com.qrmenu.service.AuthenticationService;
import com.qrmenu.service.EmailService;
import com.qrmenu.service.RateLimitService;
import com.qrmenu.service.UserService;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenConfig tokenConfig;
    private final RateLimitService rateLimitService;
    private final EmailService emailService;
    private final MeterRegistry meterRegistry;

    private static final String ACCESS_TOKEN_PREFIX = "access_token:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";
    private static final long REFRESH_TOKEN_DURATION = 30; // days
    private static final String PASSWORD_RESET_PREFIX = "password_reset:";
    private static final long PASSWORD_RESET_EXPIRATION = 1; // hours

    @Override
    @Timed(value = "auth.login", description = "Time taken to process login")
    @NewSpan("auth.login")
    public TokenResponse login(@SpanTag("auth.email") String email, String password) {
        try {
            rateLimitService.checkLoginAttempt(email);
            return userService.findByEmail(email)
                    .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                    .map(this::generateTokens)
                    .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
        } finally {
            meterRegistry.counter("auth.login.attempts").increment();
        }
    }

    @Override
    @Timed(value = "auth.refresh", description = "Time taken to refresh token")
    @NewSpan("auth.refresh")
    public TokenResponse refreshToken(@SpanTag("auth.refresh_token") String refreshToken) {
        String userId = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);
        if (userId == null) {
            throw new AuthenticationException("Invalid refresh token");
        }

        rateLimitService.checkTokenRefreshAttempt(userId);
        return userService.findById(Long.parseLong(userId))
                .map(this::generateTokens)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    @Override
    public void logout(String accessToken) {
        String userId = redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + accessToken);
        if (userId != null) {
            redisTemplate.delete(ACCESS_TOKEN_PREFIX + accessToken);
            // Remove this token from user's token set
            redisTemplate.opsForSet().remove(USER_TOKENS_PREFIX + userId, accessToken);
        }
    }

    @Override
    public void logoutAll(Long userId) {
        // Get all tokens for user and delete them

        Set<String> tokens = redisTemplate.opsForSet().members(USER_TOKENS_PREFIX + userId);

        if (tokens == null) return;

        tokens.forEach(token -> redisTemplate.delete(ACCESS_TOKEN_PREFIX + token));
        redisTemplate.delete(USER_TOKENS_PREFIX + userId);
    }

    @Override
    @Timed(value = "auth.password.reset", description = "Time taken to process password reset")
    @NewSpan("auth.password.reset.request")
    public void requestPasswordReset(@SpanTag("auth.email") String email) {
        rateLimitService.checkPasswordResetAttempt(email);
        userService.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(
                    PASSWORD_RESET_PREFIX + token,
                    user.getId().toString(),
                    PASSWORD_RESET_EXPIRATION,
                    TimeUnit.HOURS
            );
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    @Override
    @NewSpan("auth.password.reset.confirm")
    public void confirmPasswordReset(
            @SpanTag("auth.reset_token") String token,
            String newPassword) {
        String userId = redisTemplate.opsForValue().get(PASSWORD_RESET_PREFIX + token);
        if (userId == null) {
            throw new AuthenticationException("Invalid or expired password reset token");
        }

        userService.findById(Long.parseLong(userId))
                .ifPresent(user -> {
                    user.setPasswordHash(passwordEncoder.encode(newPassword));
                    userService.updateUser(user);
                    redisTemplate.delete(PASSWORD_RESET_PREFIX + token);
                    logoutAll(user.getId());
                    emailService.sendPasswordChangedEmail(user.getEmail());
                });
    }

    private TokenResponse generateTokens(User user) {
        String accessToken = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();

        // Store access token
        redisTemplate.opsForValue().set(
                ACCESS_TOKEN_PREFIX + accessToken,
                user.getId().toString(),
                tokenConfig.getExpirationDuration()
        );

        // Store refresh token
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + refreshToken,
                user.getId().toString(),
                REFRESH_TOKEN_DURATION,
                TimeUnit.DAYS
        );

        // Add access token to user's token set
        redisTemplate.opsForSet().add(
                USER_TOKENS_PREFIX + user.getId(),
                accessToken
        );

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenConfig.getExpiration())
                .tokenType("Bearer")
                .build();
    }

    @Override
    public User getCurrentUser(String token) {
        String userId = redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + token);
        if (userId == null) {
            throw new AuthenticationException("Invalid token");
        }
        return userService.findById(Long.parseLong(userId))
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    @Override
    public boolean validateToken(String token) {
        return redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + token) != null;
    }
}