package com.tyclick.chroniquesdetyrieback.auth.refreshtoken;

import com.tyclick.chroniquesdetyrieback.common.security.TokenGenerator;
import com.tyclick.chroniquesdetyrieback.common.security.TokenHasher;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenProperties refreshTokenProperties;
    private final TokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;

    /**
     * Creates a new refresh token for the given user.
     * @param user The user for whom the refresh token is being created.
     * @param rememberMe A boolean indicating whether the token should have a longer expiration time (for "remember me" functionality).
     * @return A CreatedRefreshToken object containing the raw token and the saved RefreshToken entity.
     */
    public CreatedRefreshToken create(User user, boolean rememberMe) {
        String rawToken = tokenGenerator.generate();
        String tokenHash = tokenHasher.hash(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(
                Instant.now().plus(refreshTokenProperties.getExpiration(rememberMe))
        );

        RefreshToken savedRefreshToken =
                refreshTokenRepository.save(refreshToken);

        return new CreatedRefreshToken(
                rawToken,
                savedRefreshToken
        );
    }

    /**
     * Finds a refresh token by its raw token value. The raw token is hashed before searching in the repository.
     * @param rawToken The raw token value to search for.
     * @return An Optional containing the found RefreshToken, or empty if not found.
     */
    public Optional<RefreshToken> findByRawToken(String rawToken) {
        String tokenHash = tokenHasher.hash(rawToken);

        return refreshTokenRepository.findByTokenHash(tokenHash);
    }

    /**
     * Checks if the given refresh token is valid by verifying that it has not been revoked and has not expired.
     * @param refreshToken The refresh token to be validated.
     * @return True if the refresh token is valid, false otherwise.
     */
    public boolean isValid(RefreshToken refreshToken) {
        boolean isNotRevoked = refreshToken.getRevokedAt() == null;
        boolean isNotExpired = refreshToken.getExpiresAt().isAfter(Instant.now());

        return isNotRevoked && isNotExpired;
    }

    /**
     * Revokes the given refresh token by setting its revokedAt timestamp to the current time.
     * @param refreshToken The refresh token to be revoked.
     */
    public void revoke(RefreshToken refreshToken) {
        if (refreshToken.getRevokedAt() != null) {
            return;
        }
        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Deletes all expired refresh tokens from the repository.
     * @return The number of deleted expired refresh tokens.
     */
    @Transactional
    public long deleteExpiredTokens() {
        return refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}