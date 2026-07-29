package com.tyclick.chroniquesdetyrieback.auth.refreshtoken;

import com.tyclick.chroniquesdetyrieback.common.security.TokenGenerator;
import com.tyclick.chroniquesdetyrieback.common.security.TokenHasher;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private TokenHasher tokenHasher;

    private RefreshTokenService refreshTokenService;

    private static final Duration SESSION_EXPIRATION = Duration.ofHours(12);

    private static final Duration REMEMBER_ME_EXPIRATION = Duration.ofDays(30);

    @BeforeEach
    void setUp() {
        RefreshTokenProperties properties =
                new RefreshTokenProperties(
                        SESSION_EXPIRATION,
                        REMEMBER_ME_EXPIRATION
                );

        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                properties,
                tokenGenerator,
                tokenHasher
        );
    }

    @Test
    void shouldCreateRefreshTokenWithSessionExpirationWhenRememberMeIsFalse() {
        User user = new User();

        String rawToken = "token-brut-securise";
        String tokenHash = "hash-du-token";

        when(tokenGenerator.generate()).thenReturn(rawToken);
        when(tokenHasher.hash(rawToken)).thenReturn(tokenHash);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant beforeCreation = Instant.now();

        CreatedRefreshToken result =
                refreshTokenService.create(user, false);

        Instant afterCreation = Instant.now();

        assertThat(result.rawToken()).isEqualTo(rawToken);
        assertThat(result.refreshToken()).isNotNull();

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getTokenHash()).isEqualTo(tokenHash);
        assertThat(savedToken.getTokenHash()).isNotEqualTo(rawToken);

        assertThat(savedToken.getExpiresAt())
                .isBetween(
                        beforeCreation.plus(SESSION_EXPIRATION),
                        afterCreation.plus(SESSION_EXPIRATION)
                );
    }

    @Test
    void shouldCreateRefreshTokenWithExtendedExpirationWhenRememberMeIsTrue() {
        User user = new User();

        String rawToken = "token-brut-securise";
        String tokenHash = "hash-du-token";

        when(tokenGenerator.generate()).thenReturn(rawToken);
        when(tokenHasher.hash(rawToken)).thenReturn(tokenHash);

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant beforeCreation = Instant.now();

        CreatedRefreshToken result =
                refreshTokenService.create(user, true);

        Instant afterCreation = Instant.now();

        assertThat(result.rawToken()).isEqualTo(rawToken);
        assertThat(result.refreshToken()).isNotNull();

        ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();

        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getTokenHash()).isEqualTo(tokenHash);

        assertThat(savedToken.getExpiresAt())
                .isBetween(
                        beforeCreation.plus(REMEMBER_ME_EXPIRATION),
                        afterCreation.plus(REMEMBER_ME_EXPIRATION)
                );
    }

    @Test
    void shouldFindRefreshTokenFromRawToken() {
        String rawToken = "token-brut-securise";
        String tokenHash = "hash-du-token";

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(tokenHash);

        when(tokenHasher.hash(rawToken)).thenReturn(tokenHash);
        when(refreshTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result =
                refreshTokenService.findByRawToken(rawToken);

        assertThat(result).contains(refreshToken);

        verify(tokenHasher).hash(rawToken);
        verify(refreshTokenRepository).findByTokenHash(tokenHash);
    }

    @Test
    void shouldReturnEmptyWhenRefreshTokenDoesNotExist() {
        String rawToken = "token-inconnu";
        String tokenHash = "hash-inconnu";

        when(tokenHasher.hash(rawToken)).thenReturn(tokenHash);
        when(refreshTokenRepository.findByTokenHash(tokenHash))
                .thenReturn(Optional.empty());

        Optional<RefreshToken> result =
                refreshTokenService.findByRawToken(rawToken);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenRefreshTokenIsValid() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));
        refreshToken.setRevokedAt(null);

        boolean result = refreshTokenService.isValid(refreshToken);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenRefreshTokenIsExpired() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiresAt(Instant.now().minus(Duration.ofMinutes(1)));
        refreshToken.setRevokedAt(null);

        boolean result = refreshTokenService.isValid(refreshToken);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenRefreshTokenIsRevoked() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiresAt(Instant.now().plus(Duration.ofDays(1)));
        refreshToken.setRevokedAt(Instant.now());

        boolean result = refreshTokenService.isValid(refreshToken);

        assertThat(result).isFalse();
    }

    @Test
    void shouldRevokeRefreshToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevokedAt(null);

        Instant beforeRevocation = Instant.now();

        refreshTokenService.revoke(refreshToken);

        Instant afterRevocation = Instant.now();

        assertThat(refreshToken.getRevokedAt())
                .isBetween(beforeRevocation, afterRevocation);

        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void shouldNotRevokeRefreshTokenAgainWhenAlreadyRevoked() {
        Instant existingRevocationDate = Instant.now().minusSeconds(60);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRevokedAt(existingRevocationDate);

        refreshTokenService.revoke(refreshToken);

        assertThat(refreshToken.getRevokedAt())
                .isEqualTo(existingRevocationDate);

        verify(refreshTokenRepository, never()).save(refreshToken);
    }

    @Test
    void shouldDeleteExpiredRefreshTokens() {
        when(refreshTokenRepository.deleteByExpiresAtBefore(any(Instant.class)))
                .thenReturn(3L);

        long deletedTokens = refreshTokenService.deleteExpiredTokens();

        assertThat(deletedTokens).isEqualTo(3L);

        verify(refreshTokenRepository)
                .deleteByExpiresAtBefore(any(Instant.class));
    }

}