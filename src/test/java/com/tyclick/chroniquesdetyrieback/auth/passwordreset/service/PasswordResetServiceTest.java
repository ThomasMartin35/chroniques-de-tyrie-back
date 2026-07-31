package com.tyclick.chroniquesdetyrieback.auth.passwordreset.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.tyclick.chroniquesdetyrieback.auth.passwordreset.exception.InvalidPasswordResetTokenException;
import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;

import com.tyclick.chroniquesdetyrieback.auth.passwordreset.config.PasswordResetTokenProperties;
import com.tyclick.chroniquesdetyrieback.auth.passwordreset.entity.PasswordResetToken;
import com.tyclick.chroniquesdetyrieback.auth.passwordreset.notification.PasswordResetNotificationSender;
import com.tyclick.chroniquesdetyrieback.auth.passwordreset.repository.PasswordResetTokenRepository;
import com.tyclick.chroniquesdetyrieback.common.security.TokenGenerator;
import com.tyclick.chroniquesdetyrieback.common.security.TokenHasher;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private TokenHasher tokenHasher;

    @Mock
    private PasswordResetTokenProperties passwordResetProperties;

    @Mock
    private PasswordResetNotificationSender passwordResetNotificationSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository,
                passwordResetTokenRepository,
                tokenGenerator,
                tokenHasher,
                passwordResetProperties,
                passwordResetNotificationSender,
                passwordEncoder
        );
    }

    @Test
    void shouldDoNothingWhenEmailDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("unknown@test.fr"))
                .thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset("unknown@test.fr");

        verify(userRepository).findByEmailIgnoreCase("unknown@test.fr");
        verifyNoInteractions(
                passwordResetTokenRepository,
                tokenGenerator,
                tokenHasher,
                passwordResetProperties,
                passwordResetNotificationSender
        );
    }

    @Test
    void shouldDoNothingWhenUserIsInactive() {
        var inactiveUser = new User();
        inactiveUser.setIsActive(false);

        when(userRepository.findByEmailIgnoreCase("inactive@test.fr"))
                .thenReturn(Optional.of(inactiveUser));

        passwordResetService.requestPasswordReset("inactive@test.fr");

        verify(userRepository).findByEmailIgnoreCase("inactive@test.fr");
        verifyNoInteractions(
                passwordResetTokenRepository,
                tokenGenerator,
                tokenHasher,
                passwordResetProperties,
                passwordResetNotificationSender
        );
    }

    @Test
    void shouldCreateAndSendPasswordResetTokenForActiveUser() {
        UUID userId = UUID.randomUUID();

        User activeUser = User.builder()
                .id(userId)
                .email("active@test.fr")
                .isActive(true)
                .build();

        when(userRepository.findByEmailIgnoreCase("active@test.fr"))
                .thenReturn(Optional.of(activeUser));
        when(tokenGenerator.generate())
                .thenReturn("rawToken");
        when(tokenHasher.hash("rawToken"))
                .thenReturn("hashedToken");
        when(passwordResetProperties.expiration())
                .thenReturn(Duration.ofMinutes(15));

        passwordResetService.requestPasswordReset("active@test.fr");

        verify(passwordResetTokenRepository)
                .deleteByUserId(userId);
        verify(tokenGenerator)
                .generate();
        verify(tokenHasher)
                .hash("rawToken");
        verify(passwordResetTokenRepository)
                .save(any(PasswordResetToken.class));
        verify(passwordResetNotificationSender)
                .sendPasswordResetLink("active@test.fr", "rawToken");
    }

    @Test
    void shouldRejectResetWhenTokenDoesNotExist() {
        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");

        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.empty());

        InvalidPasswordResetTokenException exception = assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordResetService.resetPassword(
                        "raw-token",
                        "Password1!",
                        "Password1!"
                )
        );

        assertEquals("Password reset token is invalid or expired", exception.getMessage());
        verify(tokenHasher).hash("raw-token");
        verify(passwordResetTokenRepository).findByTokenHash("hashed-token");

        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void shouldRejectResetWhenPasswordsDoNotMatch() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> passwordResetService.resetPassword(
                        "raw-token",
                        "Password1!",
                        "DifferentPassword1!"
                )
        );

        assertEquals("Passwords do not match", exception.getMessage());

        verifyNoInteractions(
                tokenHasher,
                passwordResetTokenRepository,
                passwordEncoder,
                userRepository
        );
    }

    @Test
    void shouldRejectResetWhenTokenIsExpired() {
        UUID userId = UUID.randomUUID();
        User activeUser = User.builder()
                .id(userId)
                .email("test@test.fr")
                .isActive(true)
                .build();

        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().minusSeconds(1))
                .user(activeUser)
                .build();

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordResetService.resetPassword(
                        "raw-token",
                        "Password1!",
                        "Password1!"
                )
        );

        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void shouldRejectResetWhenTokenIsAlreadyUsed() {
        UUID userId = UUID.randomUUID();
        User activeUser = User.builder()
                .id(userId)
                .email("test@test.fr")
                .isActive(true)
                .build();

        PasswordResetToken usedToken = PasswordResetToken.builder()
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().plusSeconds(3600))
                .usedAt(Instant.now().minusSeconds(1))
                .user(activeUser)
                .build();

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(usedToken));

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordResetService.resetPassword(
                        "raw-token",
                        "Password1!",
                        "Password1!"
                )
        );

        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void shouldRejectResetWhenUserIsInactive() {
        User inactiveUser = User.builder()
                .isActive(false)
                .build();

        PasswordResetToken validToken = PasswordResetToken.builder()
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().plusSeconds(3600))
                .user(inactiveUser)
                .build();

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(validToken));

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordResetService.resetPassword(
                        "raw-token",
                        "Password1!",
                        "Password1!"
                )
        );

        verifyNoInteractions(passwordEncoder, userRepository);
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        UUID userId = UUID.randomUUID();
        User activeUser = User.builder()
                .id(userId)
                .email("test@test.fr")
                .isActive(true)
                .build();

        PasswordResetToken validToken = PasswordResetToken.builder()
                .tokenHash("hashed-token")
                .expiresAt(Instant.now().plusSeconds(3600))
                .user(activeUser)
                .build();

        when(tokenHasher.hash("raw-token"))
                .thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token"))
                .thenReturn(Optional.of(validToken));

        when(passwordEncoder.encode("Password1!"))
                .thenReturn("encoded-password");

        passwordResetService.resetPassword(
                "raw-token",
                "Password1!",
                "Password1!"
        );

        verify(tokenHasher).hash("raw-token");
        verify(passwordResetTokenRepository)
                .findByTokenHash("hashed-token");
        verify(passwordEncoder).encode("Password1!");
        verify(userRepository).save(activeUser);
        verify(passwordResetTokenRepository).save(validToken);

        assertEquals(
                "encoded-password",
                activeUser.getPasswordHash()
        );
        assertNotNull(activeUser.getLastPasswordChangeAt());
        assertNotNull(validToken.getUsedAt());
        assertEquals(
                activeUser.getLastPasswordChangeAt(),
                validToken.getUsedAt()
        );
    }

}
