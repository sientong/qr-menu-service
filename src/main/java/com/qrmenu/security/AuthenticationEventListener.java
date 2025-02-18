package com.qrmenu.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {
    private final SecurityEventLogger securityEventLogger;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        String username = success.getAuthentication().getName();
        securityEventLogger.logEvent(
            "LOGIN_SUCCESS",
            username,
            "User successfully authenticated"
        );
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failures) {
        String username = failures.getAuthentication().getName();
        securityEventLogger.logEvent(
            "LOGIN_FAILURE",
            username,
            "Authentication failed: " + failures.getException().getMessage()
        );
    }
} 