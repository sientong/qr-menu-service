package com.qrmenu.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;

@SpringBootTest
@ActiveProfiles("test")
class RedisSessionSecurityTest {

    @Autowired
    private RedisSessionManager sessionManager;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void shouldPreventSessionFixation() {
        String fixedSessionId = "fixed-session-id";
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.RESTAURANT_ADMIN)
                .build();

        sessionManager.createSession(fixedSessionId, user);

        // Attempt to reuse the same session ID
        User user2 = User.builder()
                .id(2L)
                .email("hacker@example.com")
                .role(UserRole.RESTAURANT_ADMIN)
                .build();

        assertThrows(RuntimeException.class, () ->
            sessionManager.createSession(fixedSessionId, user2));
    }

    @Test
    void shouldPreventSessionHijacking() {
        String sessionId = UUID.randomUUID().toString();
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.RESTAURANT_ADMIN)
                .build();

        sessionManager.createSession(sessionId, user);

        // Attempt to modify session data directly in Redis
        String sessionKey = "session:" + sessionId;
        assertThrows(Exception.class, () ->
            redisTemplate.opsForValue().set(sessionKey, "{\"id\":2,\"role\":\"SUPER_ADMIN\"}"));
    }

    @Test
    void shouldHandleSessionTimeout() {
        String sessionId = UUID.randomUUID().toString();
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.RESTAURANT_ADMIN)
                .build();

        sessionManager.createSession(sessionId, user);

        // Simulate session timeout
        redisTemplate.expire("session:" + sessionId, 1, TimeUnit.MILLISECONDS);
        assertThrows(ConcurrentException.class, () -> TimeUnit.MILLISECONDS.sleep(2));

        User retrievedUser = sessionManager.getSession(sessionId);
        assertThat(retrievedUser).isNull();
    }

    @Test
    void shouldPreventConcurrentSessions() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.RESTAURANT_ADMIN)
                .build();

        // Create maximum allowed sessions
        for (int i = 0; i < 5; i++) {
            sessionManager.createSession(UUID.randomUUID().toString(), user);
        }

        // Attempt to create one more session
        assertThrows(RuntimeException.class, () ->
            sessionManager.createSession(UUID.randomUUID().toString(), user));
    }
}