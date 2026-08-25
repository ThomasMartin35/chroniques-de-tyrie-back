package com.tyclick.chroniquesdetyrieback.media.delivery.model;

import org.springframework.core.io.Resource;

public record LoadedPublicMedia(
        Resource resource,
        String mimeType,
        long sizeBytes
) {
}
