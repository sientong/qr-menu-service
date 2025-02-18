package com.qrmenu.security;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthenticationEventListenerTest {

    @Autowired
    private AuthenticationEventListener eventListener;

    @MockBean
    private SecurityEventLogger securityEventLogger;

    @MockBean
    private RedisSessionManager sessionManager;

    @Test
    void shouldLogSuccessfulAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(auth);
        eventListener.onSuccess(event);

        ArgumentCaptor<String> eventTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> detailsCaptor = ArgumentCaptor.forClass(String.class);

        verify(securityEventLogger).logEvent(
            eventTypeCaptor.capture(),
            usernameCaptor.capture(),
            detailsCaptor.capture()
        );

        assertThat(eventTypeCaptor.getValue()).isEqualTo("LOGIN_SUCCESS");
        assertThat(usernameCaptor.getValue()).isEqualTo("test@example.com");
    }

    @Test
    void shouldLogFailedAuthentication() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test@example.com");
        
        Exception ex = new Exception("Invalid credentials");
        AuthenticationFailureBadCredentialsEvent event = 
            new AuthenticationFailureBadCredentialsEvent(auth, ex);
        
        eventListener.onFailure(event);

        verify(securityEventLogger).logEvent(
            eq("LOGIN_FAILURE"),
            eq("test@example.com"),
            contains("Invalid credentials")
        );
    }
} 