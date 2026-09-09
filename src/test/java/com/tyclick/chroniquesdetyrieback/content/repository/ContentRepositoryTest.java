package com.tyclick.chroniquesdetyrieback.content.repository;

import com.tyclick.chroniquesdetyrieback.category.entity.Category;
import com.tyclick.chroniquesdetyrieback.category.repository.CategoryRepository;
import com.tyclick.chroniquesdetyrieback.config.PostgresTestContainerConfiguration;
import com.tyclick.chroniquesdetyrieback.content.entity.Content;
import com.tyclick.chroniquesdetyrieback.content.entity.ContentStatus;
import com.tyclick.chroniquesdetyrieback.content.entity.ContentType;
import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import com.tyclick.chroniquesdetyrieback.media.entity.MediaPurpose;
import com.tyclick.chroniquesdetyrieback.media.repository.MediaRepository;
import com.tyclick.chroniquesdetyrieback.tag.entity.Tag;
import com.tyclick.chroniquesdetyrieback.tag.repository.TagRepository;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.entity.UserRole;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.node.JsonNodeFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(PostgresTestContainerConfiguration.class)
@ActiveProfiles("test")
class ContentRepositoryTest {

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndReloadContentWithItsRelationsAndJsonBody() {
        String uniqueSuffix = UUID.randomUUID().toString();

        User author = userRepository.save(User.builder()
                .username("author-" + uniqueSuffix)
                .email("author-" + uniqueSuffix + "@example.com")
                .passwordHash("test-password-hash")
                .role(UserRole.ROLE_EDITOR)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("News " + uniqueSuffix)
                .slug("news-" + uniqueSuffix)
                .description("News category used by the persistence test")
                .build());

        Tag tag = tagRepository.save(Tag.builder()
                .name("Guild Wars " + uniqueSuffix)
                .slug("guild-wars-" + uniqueSuffix)
                .description("Tag used by the persistence test")
                .build());

        var body = JsonNodeFactory.instance.objectNode()
                .put("type", "doc")
                .set("content", JsonNodeFactory.instance.arrayNode());

        Content content = Content.builder()
                .title("Guild Wars 3 is announced")
                .slug("guild-wars-3-is-announced-" + uniqueSuffix)
                .excerpt("A persistence test for the editorial content foundation.")
                .body(body)
                .type(ContentType.NEWS)
                .author(author)
                .category(category)
                .tags(new HashSet<>(Set.of(tag)))
                .readingTime(4)
                .build();

        Content savedContent = contentRepository.saveAndFlush(content);
        UUID contentId = savedContent.getId();

        entityManager.clear();

        Content reloadedContent = contentRepository.findById(contentId).orElseThrow();

        assertThat(reloadedContent.getTitle()).isEqualTo("Guild Wars 3 is announced");
        assertThat(reloadedContent.getStatus()).isEqualTo(ContentStatus.DRAFT);
        assertThat(reloadedContent.getType()).isEqualTo(ContentType.NEWS);
        assertThat(reloadedContent.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(reloadedContent.getCategory().getId()).isEqualTo(category.getId());
        assertThat(reloadedContent.getTags())
                .extracting(Tag::getId)
                .containsExactly(tag.getId());
        assertThat(reloadedContent.getBody().path("type").asString()).isEqualTo("doc");
        assertThat(reloadedContent.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldPersistIncompleteDraft() {
        String uniqueSuffix = UUID.randomUUID().toString();

        User author = userRepository.save(User.builder()
                .username("author-" + uniqueSuffix)
                .email("author-" + uniqueSuffix + "@example.com")
                .passwordHash("test-password-hash")
                .role(UserRole.ROLE_EDITOR)
                .build());

        Content content = Content.builder()
                .title("Incomplete draft")
                .slug("incomplete-draft-" + uniqueSuffix)
                .type(ContentType.NEWS)
                .author(author)
                .build();

        Content savedContent = contentRepository.saveAndFlush(content);
        UUID contentId = savedContent.getId();

        entityManager.clear();

        Content reloadedContent = contentRepository.findById(contentId).orElseThrow();
        assertThat(reloadedContent.getTitle()).isEqualTo("Incomplete draft");
        assertThat(reloadedContent.getStatus()).isEqualTo(ContentStatus.DRAFT);
        assertThat(reloadedContent.getType()).isEqualTo(ContentType.NEWS);
        assertThat(reloadedContent.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(reloadedContent.getExcerpt()).isNull();
        assertThat(reloadedContent.getBody()).isNull();
        assertThat(reloadedContent.getCategory()).isNull();
        assertThat(reloadedContent.getFeaturedImage()).isNull();
        assertThat(reloadedContent.getFeaturedImageAltText()).isNull();
        assertThat(reloadedContent.getTags()).isEmpty();
        assertThat(reloadedContent.getReadingTime()).isNull();
        assertThat(reloadedContent.getSubmittedAt()).isNull();
        assertThat(reloadedContent.getPublishedAt()).isNull();
    }

    @Test
    void shouldRejectDuplicateContentSlug() {
        String uniqueSuffix = UUID.randomUUID().toString();

        User author = userRepository.save(User.builder()
                .username("author-" + uniqueSuffix)
                .email("author-" + uniqueSuffix + "@example.com")
                .passwordHash("test-password-hash")
                .role(UserRole.ROLE_EDITOR)
                .build());

        Content content1 = Content.builder()
                .title("First content")
                .slug("duplicate-slug-" + uniqueSuffix)
                .type(ContentType.NEWS)
                .author(author)
                .build();

        contentRepository.saveAndFlush(content1);

        Content content2 = Content.builder()
                .title("Second content")
                .slug("duplicate-slug-" + uniqueSuffix)
                .type(ContentType.NEWS)
                .author(author)
                .build();

        assertThatThrownBy(() -> contentRepository.saveAndFlush(content2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectNonPositiveReadingTime() {
        String uniqueSuffix = UUID.randomUUID().toString();

        User author = userRepository.save(User.builder()
                .username("author-" + uniqueSuffix)
                .email("author-" + uniqueSuffix + "@example.com")
                .passwordHash("test-password-hash")
                .role(UserRole.ROLE_EDITOR)
                .build());

        Content content = Content.builder()
                .title("Invalid reading time")
                .slug("invalid-reading-time-" + uniqueSuffix)
                .type(ContentType.NEWS)
                .author(author)
                .readingTime(0)
                .build();

        assertThatThrownBy(() -> contentRepository.saveAndFlush(content))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldPersistLifecycleMetadata() {
        String uniqueSuffix = UUID.randomUUID().toString();

        User author = createUser("author", UserRole.ROLE_EDITOR, uniqueSuffix);
        User reviewer = createUser("reviewer", UserRole.ROLE_ADMIN, uniqueSuffix);
        Instant submittedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Instant reviewedAt = submittedAt.plusSeconds(60);
        Instant publishedAt = reviewedAt.plusSeconds(60);
        Instant archivedAt = publishedAt.plusSeconds(60);

        Content content = Content.builder()
                .title("Content with lifecycle metadata")
                .slug("content-with-lifecycle-metadata-" + uniqueSuffix)
                .type(ContentType.NEWS)
                .status(ContentStatus.ARCHIVED)
                .author(author)
                .submittedAt(submittedAt)
                .reviewNote("Editorial review completed")
                .reviewedBy(reviewer)
                .reviewedAt(reviewedAt)
                .publishedBy(reviewer)
                .publishedAt(publishedAt)
                .archivedBy(reviewer)
                .archivedAt(archivedAt)
                .build();

        UUID contentId = contentRepository.saveAndFlush(content).getId();
        entityManager.clear();

        Content reloadedContent = contentRepository.findById(contentId).orElseThrow();

        assertThat(reloadedContent.getStatus()).isEqualTo(ContentStatus.ARCHIVED);
        assertThat(reloadedContent.getSubmittedAt()).isEqualTo(submittedAt);
        assertThat(reloadedContent.getReviewNote()).isEqualTo("Editorial review completed");
        assertThat(reloadedContent.getReviewedBy().getId()).isEqualTo(reviewer.getId());
        assertThat(reloadedContent.getReviewedAt()).isEqualTo(reviewedAt);
        assertThat(reloadedContent.getPublishedBy().getId()).isEqualTo(reviewer.getId());
        assertThat(reloadedContent.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(reloadedContent.getArchivedBy().getId()).isEqualTo(reviewer.getId());
        assertThat(reloadedContent.getArchivedAt()).isEqualTo(archivedAt);
    }

    @Test
    void shouldPersistOptionalFeaturedImageRelationship() {
        String uniqueSuffix = UUID.randomUUID().toString();
        User author = createUser("author", UserRole.ROLE_EDITOR, uniqueSuffix);

        Media featuredImage = mediaRepository.save(Media.builder()
                .storageKey("test/featured-" + uniqueSuffix + ".webp")
                .originalFilename("featured.webp")
                .mimeType("image/webp")
                .sizeBytes(1_024L)
                .purpose(MediaPurpose.AVATAR)
                .altText("A Guild Wars landscape")
                .uploadedBy(author)
                .build());

        Content content = Content.builder()
                .title("Content with featured image")
                .slug("content-with-featured-image-" + uniqueSuffix)
                .type(ContentType.NEWS)
                .author(author)
                .featuredImage(featuredImage)
                .featuredImageAltText("A Guild Wars landscape")
                .build();

        UUID contentId = contentRepository.saveAndFlush(content).getId();
        entityManager.clear();

        Content reloadedContent = contentRepository.findById(contentId).orElseThrow();

        assertThat(reloadedContent.getFeaturedImage().getId()).isEqualTo(featuredImage.getId());
        assertThat(reloadedContent.getFeaturedImageAltText()).isEqualTo("A Guild Wars landscape");
    }

    @Test
    void shouldRejectDuplicateContentTagAssociation() {
        String uniqueSuffix = UUID.randomUUID().toString();
        User author = createUser("author", UserRole.ROLE_EDITOR, uniqueSuffix);
        Tag tag = tagRepository.save(Tag.builder()
                .name("Duplicate association " + uniqueSuffix)
                .slug("duplicate-association-" + uniqueSuffix)
                .build());

        Content content = Content.builder()
                .title("Content with a tag")
                .slug("content-with-a-tag-" + uniqueSuffix)
                .type(ContentType.NEWS)
                .author(author)
                .tags(new HashSet<>(Set.of(tag)))
                .build();

        Content savedContent = contentRepository.saveAndFlush(content);

        assertThatThrownBy(() -> entityManager.createNativeQuery("""
                        INSERT INTO content_tags (content_id, tag_id)
                        VALUES (:contentId, :tagId)
                        """)
                .setParameter("contentId", savedContent.getId())
                .setParameter("tagId", tag.getId())
                .executeUpdate())
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    void shouldRejectInvalidContentType() {
        String uniqueSuffix = UUID.randomUUID().toString();
        User author = createUser("author", UserRole.ROLE_EDITOR, uniqueSuffix);

        assertThatThrownBy(() -> insertContentWithRawEnums(
                "invalid-type-" + uniqueSuffix,
                "INVALID_TYPE",
                ContentStatus.DRAFT.name(),
                author.getId()
        )).isInstanceOf(PersistenceException.class);
    }

    @Test
    void shouldRejectInvalidContentStatus() {
        String uniqueSuffix = UUID.randomUUID().toString();
        User author = createUser("author", UserRole.ROLE_EDITOR, uniqueSuffix);

        assertThatThrownBy(() -> insertContentWithRawEnums(
                "invalid-status-" + uniqueSuffix,
                ContentType.NEWS.name(),
                "INVALID_STATUS",
                author.getId()
        )).isInstanceOf(PersistenceException.class);
    }

    private User createUser(String prefix, UserRole role, String uniqueSuffix) {
        return userRepository.saveAndFlush(User.builder()
                .username(prefix + "-" + uniqueSuffix)
                .email(prefix + "-" + uniqueSuffix + "@example.com")
                .passwordHash("test-password-hash")
                .role(role)
                .build());
    }

    private void insertContentWithRawEnums(String slug, String type, String status, UUID authorId) {
        entityManager.createNativeQuery("""
                        INSERT INTO contents (
                            id, title, slug, type, status, author_id, created_at
                        ) VALUES (
                            :id, :title, :slug, :type, :status, :authorId, :createdAt
                        )
                        """)
                .setParameter("id", UUID.randomUUID())
                .setParameter("title", "Content with invalid enum")
                .setParameter("slug", slug)
                .setParameter("type", type)
                .setParameter("status", status)
                .setParameter("authorId", authorId)
                .setParameter("createdAt", Instant.now())
                .executeUpdate();
    }
}
