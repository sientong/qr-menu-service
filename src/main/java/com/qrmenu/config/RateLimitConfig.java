package com.qrmenu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "application.rate-limit")
@Data
public class RateLimitConfig {
    private int loginAttemptsPerHour = 5;
    private int passwordResetAttemptsPerDay = 3;
    private int tokenRefreshAttemptsPerHour = 10;
    private int requestsPerMinute;

    // Getters and setters
    public int getLoginAttemptsPerHour() {
        return loginAttemptsPerHour;
    }

    public void setLoginAttemptsPerHour(int loginAttemptsPerHour) {
        this.loginAttemptsPerHour = loginAttemptsPerHour;
    }

    public int getPasswordResetAttemptsPerDay() {
        return passwordResetAttemptsPerDay;
    }

    public void setPasswordResetAttemptsPerDay(int passwordResetAttemptsPerDay) {
        this.passwordResetAttemptsPerDay = passwordResetAttemptsPerDay;
    }

    public int getTokenRefreshAttemptsPerHour() {
        return tokenRefreshAttemptsPerHour;
    }

    public void setTokenRefreshAttemptsPerHour(int tokenRefreshAttemptsPerHour) {
        this.tokenRefreshAttemptsPerHour = tokenRefreshAttemptsPerHour;
    }
} 