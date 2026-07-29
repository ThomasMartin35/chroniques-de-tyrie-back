package com.tyclick.chroniquesdetyrieback.auth.jwt;

import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.entity.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET_KEY =
            "test-secret-key-that-is-at-least-32-bytes-long";
    private static final long EXPIRATION_TIME = 900_000L;

    private final JwtService jwtService =
            new JwtService(SECRET_KEY, EXPIRATION_TIME);

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        User user = User.builder()
                .email("thomas@test.fr")
                .role(UserRole.ROLE_MEMBER)
                .isActive(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("thomas@test.fr", jwtService.extractUsername(token));
    }

}
