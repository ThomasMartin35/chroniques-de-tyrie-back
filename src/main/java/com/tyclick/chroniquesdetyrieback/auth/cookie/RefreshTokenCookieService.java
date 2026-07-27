package com.tyclick.chroniquesdetyrieback.auth.cookie;

import com.tyclick.chroniquesdetyrieback.auth.refreshtoken.RefreshTokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    private static final String COOKIE_NAME = "refresh_token";

    private final RefreshTokenProperties refreshTokenProperties;

    public ResponseCookie create(String rawRefreshToken) {
        return ResponseCookie.from(COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(refreshTokenProperties.expiration())
                .build();
    }

    public ResponseCookie delete() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }
}