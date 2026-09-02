package com.tyclick.chroniquesdetyrieback.content.entity;

import com.tyclick.chroniquesdetyrieback.category.entity.Category;
import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import com.tyclick.chroniquesdetyrieback.tag.entity.Tag;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "slug", nullable = false, length = 220, unique = true)
    private String slug;

    @Column(name = "excerpt", length = 500)
    private String excerpt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "body", columnDefinition = "jsonb")
    private JsonNode body;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ContentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "featured_image_alt_text", length = 250)
    private String featuredImageAltText;

    @Column(name = "reading_time")
    private Integer readingTime;

    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "featured_image_id", unique = true)
    private Media featuredImage;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "content_tags",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by_id")
    private User publishedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archived_by_id")
    private User archivedBy;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
