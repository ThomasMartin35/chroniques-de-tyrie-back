package com.tyclick.chroniquesdetyrieback.media.avatar.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "media.avatar")
public record AvatarImageProperties(

        @NotNull
        DataSize maxFileSize,

        @Min(1)
        int maxWidth,

        @Min(1)
        int maxHeight,

        @Min(1)
        long maxPixelCount,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        float webpQuality

) {
}
