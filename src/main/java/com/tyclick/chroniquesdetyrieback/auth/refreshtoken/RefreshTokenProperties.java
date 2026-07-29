package com.tyclick.chroniquesdetyrieback.auth.refreshtoken;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.refresh-token")
public record RefreshTokenProperties(
        Duration sessionExpiration,
        Duration rememberMeExpiration
) {

    public Duration getExpiration(boolean rememberMe) {
        return rememberMe
                ? rememberMeExpiration
                : sessionExpiration;
    }
}