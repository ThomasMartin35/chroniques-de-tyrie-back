package com.tyclick.chroniquesdetyrieback.auth.refreshtoken;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RefreshTokenProperties.class)
public class RefreshTokenConfiguration {
}