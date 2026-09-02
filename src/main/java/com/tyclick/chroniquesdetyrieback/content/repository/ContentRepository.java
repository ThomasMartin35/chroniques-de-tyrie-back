package com.tyclick.chroniquesdetyrieback.content.repository;

import com.tyclick.chroniquesdetyrieback.content.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {
}
