package com.tyclick.chroniquesdetyrieback.auth.refreshtoken.model;

import com.tyclick.chroniquesdetyrieback.auth.refreshtoken.entity.RefreshToken;

public record CreatedRefreshToken(
        String rawToken,
        RefreshToken refreshToken
) {
}
