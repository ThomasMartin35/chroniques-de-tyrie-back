package com.tyclick.chroniquesdetyrieback.media.storage.config;

import java.nio.file.Path;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "media.storage")
public record MediaStorageProperties(
        @NotNull Path rootDirectory
) {
}
