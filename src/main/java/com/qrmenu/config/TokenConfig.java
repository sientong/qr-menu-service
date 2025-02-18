package com.qrmenu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "application.security.token")
public class TokenConfig {
    private long expiration = 86400000; // 24 hours in milliseconds

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public Duration getExpirationDuration() {
        return Duration.ofMillis(expiration);
    }
} 