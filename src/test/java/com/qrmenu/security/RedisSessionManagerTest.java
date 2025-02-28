package com.qrmenu.security;

import com.qrmenu.model.User;
import com.qrmenu.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisSessionManagerTest {

    @Autowired
    private RedisSessionManager sessionManager;

    private User testUser;
    private String sessionId;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(UserRole.RESTAURANT_ADMIN)
                .build();
        sessionId = "test-session-id";
    }

    @Test
    void shouldCreateAndRetrieveSession() {
        sessionManager.createSession(sessionId, testUser);
        User retrievedUser = sessionManager.getSession(sessionId);
        
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getId()).isEqualTo(testUser.getId());
        assertThat(retrievedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(retrievedUser.getRole()).isEqualTo(testUser.getRole());
    }

    @Test
    void shouldInvalidateSession() {
        sessionManager.createSession(sessionId, testUser);
        sessionManager.invalidateSession(sessionId);
        
        User retrievedUser = sessionManager.getSession(sessionId);
        assertThat(retrievedUser).isNull();
    }

    @Test
    void shouldInvalidateAllUserSessions() {
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        
        sessionManager.createSession(sessionId1, testUser);
        sessionManager.createSession(sessionId2, testUser);
        
        sessionManager.invalidateUserSessions(testUser.getId());
        
        assertThat(sessionManager.getSession(sessionId1)).isNull();
        assertThat(sessionManager.getSession(sessionId2)).isNull();
    }

    @Test
    void shouldHandleNonExistentSession() {
        User retrievedUser = sessionManager.getSession("non-existent-session");
        assertThat(retrievedUser).isNull();
    }

    @Test
    void shouldHandleInvalidUserJson() {
        // This test requires direct Redis access to simulate corruption
        // Implementation depends on your Redis template configuration
    }
} 