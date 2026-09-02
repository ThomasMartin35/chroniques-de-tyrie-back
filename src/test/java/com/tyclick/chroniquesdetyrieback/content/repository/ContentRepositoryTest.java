package com.tyclick.chroniquesdetyrieback.content.repository;

import com.tyclick.chroniquesdetyrieback.category.entity.Category;
import com.tyclick.chroniquesdetyrieback.category.repository.CategoryRepository;
import com.tyclick.chroniquesdetyrieback.content.entity.Content;
import com.tyclick.chroniquesdetyrieback.content.entity.ContentStatus;
import com.tyclick.chroniquesdetyrieback.content.entity.ContentType;
import com.tyclick.chroniquesdetyrieback.tag.entity.Tag;
import com.tyclick.chroniquesdetyrieback.tag.repository.TagRepository;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.entity.UserRole;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
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
}
