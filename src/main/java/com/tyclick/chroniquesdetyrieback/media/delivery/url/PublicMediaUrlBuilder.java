package com.tyclick.chroniquesdetyrieback.media.delivery.url;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.tyclick.chroniquesdetyrieback.media.entity.Media;

@Component
public class PublicMediaUrlBuilder {

    public String build(UUID mediaId) {
        if (mediaId == null) {
            return null;
        }

        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/media/{mediaId}")
                .buildAndExpand(mediaId)
                .toUriString();
    }

    public String build(Media media) {
        if (media == null) {
            return null;
        }

        return build(media.getId());
    }
}