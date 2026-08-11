package com.tyclick.chroniquesdetyrieback.media.repository;

import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
}
