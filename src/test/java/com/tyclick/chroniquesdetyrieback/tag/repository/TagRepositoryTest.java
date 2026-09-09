package com.tyclick.chroniquesdetyrieback.tag.repository;

import com.tyclick.chroniquesdetyrieback.config.PostgresTestContainerConfiguration;
import com.tyclick.chroniquesdetyrieback.tag.entity.Tag;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import(PostgresTestContainerConfiguration.class)
@ActiveProfiles("test")
public class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveTag() {
        Tag tag = Tag.builder()
                .name("Test Tag")
                .slug("test-tag")
                .description("This is a test tag.")
                .build();

        Tag savedTag = tagRepository.saveAndFlush(tag);
        UUID tagId = savedTag.getId();

        entityManager.clear();

        Tag retrievedTag = tagRepository.findById(tagId)
                .orElseThrow();

        assertThat(retrievedTag.getId()).isEqualTo(tagId);
        assertThat(retrievedTag.getName()).isEqualTo("Test Tag");
        assertThat(retrievedTag.getSlug()).isEqualTo("test-tag");
        assertThat(retrievedTag.getDescription()).isEqualTo("This is a test tag.");
        assertThat(retrievedTag.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateNormalizedTagName() {
        Tag firstTag = Tag.builder()
                .name("Guild Wars 3")
                .slug("guild-wars-3")
                .build();

        tagRepository.saveAndFlush(firstTag);

        Tag secondTag = Tag.builder()
                .name("guild wars 3")
                .slug("guild-wars-3-secondary")
                .build();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> tagRepository.saveAndFlush(secondTag)
        );
    }
}
