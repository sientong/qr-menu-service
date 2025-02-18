package com.qrmenu.security;

import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TOKEN_PREFIX = "auth:token:";
    private static final long TOKEN_EXPIRATION = 24; // hours

    public String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        String key = TOKEN_PREFIX + token;
        String value = String.format("%d:%s:%s", user.getId(), user.getEmail(), user.getRole());
        
        redisTemplate.opsForValue().set(key, value, TOKEN_EXPIRATION, TimeUnit.HOURS);
        return token;
    }

    public boolean validateToken(String token) {
        return redisTemplate.hasKey(TOKEN_PREFIX + token);
    }

    public void invalidateToken(String token) {
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    public User getUserFromToken(String token) {
        String value = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (value == null) {
            return null;
        }

        String[] parts = value.split(":");
        return User.builder()
                .id(Long.parseLong(parts[0]))
                .email(parts[1])
                .role(UserRole.valueOf(parts[2]))
                .build();
    }

    public void refreshToken(String token) {
        String key = TOKEN_PREFIX + token;
        String value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            redisTemplate.expire(key, TOKEN_EXPIRATION, TimeUnit.HOURS);
        }
    }
} 