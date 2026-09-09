package com.tyclick.chroniquesdetyrieback.category.repository;

import com.tyclick.chroniquesdetyrieback.category.entity.Category;
import com.tyclick.chroniquesdetyrieback.config.PostgresTestContainerConfiguration;
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
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndRetrieveCategory() {
        Category category = Category.builder()
                .name("Test Category")
                .slug("test-category")
                .description("This is a test category.")
                .build();

        Category savedCategory = categoryRepository.saveAndFlush(category);
        UUID categoryId = savedCategory.getId();

        entityManager.clear();

        Category retrievedCategory = categoryRepository.findById(categoryId)
                .orElseThrow();

        assertThat(retrievedCategory.getId()).isEqualTo(categoryId);
        assertThat(retrievedCategory.getName()).isEqualTo("Test Category");
        assertThat(retrievedCategory.getSlug()).isEqualTo("test-category");
        assertThat(retrievedCategory.getDescription())
                .isEqualTo("This is a test category.");
        assertThat(retrievedCategory.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateNormalizedCategoryName() {
        Category firstCategory = Category.builder()
                .name("Duplicate Category")
                .slug("duplicate-category")
                .build();

        categoryRepository.saveAndFlush(firstCategory);

        Category secondCategory = Category.builder()
                .name("duplicate category")
                .slug("duplicate-category-2")
                .build();

        assertThrows(
                DataIntegrityViolationException.class,
                () -> categoryRepository.saveAndFlush(secondCategory)
        );
    }
}