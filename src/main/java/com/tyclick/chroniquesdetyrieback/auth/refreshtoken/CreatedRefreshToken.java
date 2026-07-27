package com.tyclick.chroniquesdetyrieback.auth.refreshtoken;

public record CreatedRefreshToken(
        String rawToken,
        RefreshToken refreshToken
) {
}