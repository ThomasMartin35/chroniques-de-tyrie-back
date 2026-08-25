package com.tyclick.chroniquesdetyrieback.media.delivery.service;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.tyclick.chroniquesdetyrieback.media.delivery.exception.PublicMediaNotFoundException;
import com.tyclick.chroniquesdetyrieback.media.delivery.model.LoadedPublicMedia;
import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import com.tyclick.chroniquesdetyrieback.media.entity.MediaPurpose;
import com.tyclick.chroniquesdetyrieback.media.repository.MediaRepository;
import com.tyclick.chroniquesdetyrieback.media.storage.MediaStorage;
import com.tyclick.chroniquesdetyrieback.media.storage.exception.MediaFileNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicMediaService {

    private final MediaRepository mediaRepository;
    private final MediaStorage mediaStorage;

    public LoadedPublicMedia loadPublicMedia(UUID mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(PublicMediaNotFoundException::new);

        if (media.getPurpose() != MediaPurpose.AVATAR) {
            throw new PublicMediaNotFoundException();
        }

        Resource resource;

        try {
            resource = mediaStorage.load(media.getStorageKey());
        } catch (MediaFileNotFoundException exception) {
            throw new PublicMediaNotFoundException();
        }

        return new LoadedPublicMedia(
                resource,
                media.getMimeType(),
                media.getSizeBytes()
        );
    }
}