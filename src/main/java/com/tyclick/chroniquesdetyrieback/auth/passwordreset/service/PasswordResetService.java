package com.tyclick.chroniquesdetyrieback.auth.passwordreset.service;

import java.time.Instant;

import com.tyclick.chroniquesdetyrieback.auth.passwordreset.exception.InvalidPasswordResetTokenException;
import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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

    @Transactional
    public void resetPassword(String rawToken, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException("Passwords do not match");
        }
        String tokenHash = tokenHasher.hash(rawToken);
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        Instant now = Instant.now();

        if (passwordResetToken.getUsedAt() != null
                || !passwordResetToken.getExpiresAt().isAfter(now)
                || !Boolean.TRUE.equals(
                        passwordResetToken.getUser().getIsActive()
                )) {
            throw new InvalidPasswordResetTokenException();
        }

        var user = passwordResetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setLastPasswordChangeAt(now);

        passwordResetToken.setUsedAt(now);

        userRepository.save(user);
        passwordResetTokenRepository.save(passwordResetToken);
    }
}
