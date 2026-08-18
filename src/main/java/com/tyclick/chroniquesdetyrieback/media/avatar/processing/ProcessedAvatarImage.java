package com.tyclick.chroniquesdetyrieback.media.avatar.processing;

public record ProcessedAvatarImage(
        byte[] content,
        String mimeType,
        String extension,
        int width,
        int height
) {

    public long sizeBytes() {
        return content.length;
    }
}