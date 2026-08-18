package com.tyclick.chroniquesdetyrieback.media.avatar.event;

public record AvatarReplacementEvent(
        String newStorageKey,
        String previousStorageKey
) {
}
