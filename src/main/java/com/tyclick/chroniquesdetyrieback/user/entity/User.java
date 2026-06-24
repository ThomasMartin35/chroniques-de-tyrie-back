package com.tyclick.chroniquesdetyrieback.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private Boolean isActive = true;

    // RELATIONS //

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avatar_id")
    private Media avatar;

    @OneToMany(mappedBy = "author")
    private Set<Content> contents = new HashSet<>();

    @OneToMany(mappedBy = "author")
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "reportedBy")
    private Set<CommentReport> reports = new HashSet<>();

    @OneToMany(mappedBy = "uploadedBy")
    private Set<Media> uploadedMedia = new HashSet<>();

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
