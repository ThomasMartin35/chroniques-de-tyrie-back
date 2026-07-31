package com.tyclick.chroniquesdetyrieback.auth.passwordreset.service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository,
                passwordResetTokenRepository,
                tokenGenerator,
                tokenHasher,
                passwordResetProperties,
                passwordResetNotificationSender
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

}
