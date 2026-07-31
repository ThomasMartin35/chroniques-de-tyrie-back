package com.tyclick.chroniquesdetyrieback.auth.passwordreset.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tyclick.chroniquesdetyrieback.auth.passwordreset.config.PasswordResetTokenProperties;
import com.tyclick.chroniquesdetyrieback.auth.passwordreset.entity.PasswordResetToken;
import com.tyclick.chroniquesdetyrieback.auth.passwordreset.notification.PasswordResetNotificationSender;
import com.tyclick.chroniquesdetyrieback.auth.passwordreset.repository.PasswordResetTokenRepository;
import com.tyclick.chroniquesdetyrieback.common.security.TokenGenerator;
import com.tyclick.chroniquesdetyrieback.common.security.TokenHasher;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final TokenHasher tokenHasher;
    private final PasswordResetTokenProperties passwordResetProperties;
    private final PasswordResetNotificationSender passwordResetNotificationSender;

    @Transactional
    public void requestPasswordReset(String email) {
        var user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null || !Boolean.TRUE.equals(user.getIsActive())) {
            // User not found or not active, do not proceed with password reset
            return;
        }

        passwordResetTokenRepository.deleteByUserId(user.getId());

        var rawToken = tokenGenerator.generate();
        var tokenHash = tokenHasher.hash(rawToken);

        Instant expiresAt = Instant.now().plus(passwordResetProperties.expiration());

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .user(user)
                .build();
        passwordResetTokenRepository.save(passwordResetToken);

        passwordResetNotificationSender.sendPasswordResetLink(user.getEmail(), rawToken);
    }
}
