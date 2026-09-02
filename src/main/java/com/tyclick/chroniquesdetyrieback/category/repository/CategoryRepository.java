package com.tyclick.chroniquesdetyrieback.category.repository;

import com.tyclick.chroniquesdetyrieback.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
