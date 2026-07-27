package com.tyclick.chroniquesdetyrieback.auth.model;

import com.tyclick.chroniquesdetyrieback.auth.dto.response.LoginResponse;

public record LoginResult(
        LoginResponse response,
        String rawRefreshToken
) {
}