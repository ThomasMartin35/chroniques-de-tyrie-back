package com.tyclick.chroniquesdetyrieback.auth.passwordreset.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tyclick.chroniquesdetyrieback.auth.passwordreset.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    long deleteByUserId(UUID userId);

    long deleteByExpiresAtBefore(Instant date);
}
