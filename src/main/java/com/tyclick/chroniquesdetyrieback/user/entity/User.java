package com.tyclick.chroniquesdetyrieback.user.entity;

import java.time.Instant;
import java.util.UUID;

import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // ATTRIBUTES //

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    @JsonIgnore // Exclude passwordHash from JSON serialization
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    @Column(name = "biography", columnDefinition = "TEXT")
    private String biography;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // RELATIONS //

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id", unique = true)
    private Media avatar;

    // TIMESTAMPS //

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_password_change_at")
    private Instant lastPasswordChangeAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
