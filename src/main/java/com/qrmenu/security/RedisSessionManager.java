package com.qrmenu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qrmenu.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisSessionManager {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user:sessions:";
    private static final long SESSION_TIMEOUT = 30; // minutes

    public void createSession(String sessionId, User user) {
        try {
            String userKey = SESSION_PREFIX + sessionId;
            String userJson = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(userKey, userJson, SESSION_TIMEOUT, TimeUnit.MINUTES);
            
            String userSessionsKey = USER_SESSIONS_PREFIX + user.getId();
            redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        } catch (Exception e) {
            throw new RuntimeException("Error creating session", e);
        }
    }

    public User getSession(String sessionId) {
        try {
            String userJson = redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
            if (userJson == null) {
                return null;
            }
            redisTemplate.expire(SESSION_PREFIX + sessionId, SESSION_TIMEOUT, TimeUnit.MINUTES);
            return objectMapper.readValue(userJson, User.class);
        } catch (Exception e) {
            throw new RuntimeException("Error getting session", e);
        }
    }

    public void invalidateSession(String sessionId) {
        String userJson = redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        if (userJson != null) {
            try {
                User user = objectMapper.readValue(userJson, User.class);
                String userSessionsKey = USER_SESSIONS_PREFIX + user.getId();
                redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
            } catch (Exception e) {
                throw new RuntimeException("Error invalidating session", e);
            }
        }
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }

    public void invalidateUserSessions(Long userId) {
        String userSessionsKey = USER_SESSIONS_PREFIX + userId;
        Set<String> sessions = redisTemplate.opsForSet().members(userSessionsKey);
        if (sessions != null) {
            sessions.forEach(this::invalidateSession);
        }
        redisTemplate.delete(userSessionsKey);
    }
} 