package com.tyclick.chroniquesdetyrieback.auth.passwordreset.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.password-reset-token")
public record PasswordResetTokenProperties(
        Duration expiration
) {
}
