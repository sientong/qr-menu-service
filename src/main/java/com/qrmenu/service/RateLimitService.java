package com.qrmenu.service;

import com.qrmenu.config.RateLimitConfig;
import com.qrmenu.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitConfig rateLimitConfig;

    private static final String LOGIN_ATTEMPTS_PREFIX = "login_attempts:";
    private static final String PASSWORD_RESET_ATTEMPTS_PREFIX = "password_reset_attempts:";
    private static final String TOKEN_REFRESH_ATTEMPTS_PREFIX = "token_refresh_attempts:";
    private static final String RATE_LIMIT_PREFIX = "rate:limit:";
    private static final int MAX_REQUESTS = 100;
    private static final int WINDOW_MINUTES = 1;


    public void checkLoginAttempt(String email) {
        String key = LOGIN_ATTEMPTS_PREFIX + email;
        checkRateLimit(key, rateLimitConfig.getLoginAttemptsPerHour(), 1, TimeUnit.HOURS,
                "Too many login attempts. Please try again later.");
    }

    public void checkPasswordResetAttempt(String email) {
        String key = PASSWORD_RESET_ATTEMPTS_PREFIX + email;
        checkRateLimit(key, rateLimitConfig.getPasswordResetAttemptsPerDay(), 1, TimeUnit.DAYS,
                "Too many password reset attempts. Please try again tomorrow.");
    }

    public void checkTokenRefreshAttempt(String userId) {
        String key = TOKEN_REFRESH_ATTEMPTS_PREFIX + userId;
        checkRateLimit(key, rateLimitConfig.getTokenRefreshAttemptsPerHour(), 1, TimeUnit.HOURS,
                "Too many token refresh attempts. Please try again later.");
    }

    public void checkRequest(String clientIp, String path) {
        String key = "request_limit:" + clientIp + ":" + path;
        checkRateLimit(key, rateLimitConfig.getRequestsPerMinute(), 1, TimeUnit.MINUTES,
                "Too many requests. Please try again later.");
    }

    private void checkRateLimit(String key, int limit, long time, TimeUnit timeUnit, String message) {
        Long attempts = stringRedisTemplate.opsForValue().increment(key);
        
        if (attempts == null) {
            attempts = 1L;
        }

        if (attempts == 1) {
            stringRedisTemplate.expire(key, time, timeUnit);
        }
        
        if (attempts > limit) {
            throw new RateLimitExceededException(message);
        }
    }

    public boolean tryAcquire(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        Long count = stringRedisTemplate.opsForValue().increment(redisKey);
        
        if (count == null) {
            return false;
        }
        
        if (count.equals(1L)) {
            stringRedisTemplate.expire(redisKey, WINDOW_MINUTES, TimeUnit.MINUTES);
        }
        
        return count <= MAX_REQUESTS;
    }
} 