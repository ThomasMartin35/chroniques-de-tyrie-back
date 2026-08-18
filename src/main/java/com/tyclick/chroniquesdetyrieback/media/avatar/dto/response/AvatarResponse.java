package com.tyclick.chroniquesdetyrieback.media.avatar.dto.response;

import java.util.UUID;

public record AvatarResponse(
        UUID mediaId,
        String avatarUrl
) {
}
