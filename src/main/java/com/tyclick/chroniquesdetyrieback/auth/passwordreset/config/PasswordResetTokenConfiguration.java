package com.tyclick.chroniquesdetyrieback.auth.passwordreset.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PasswordResetTokenProperties.class)
public class PasswordResetTokenConfiguration {
}
