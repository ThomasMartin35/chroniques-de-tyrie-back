package com.tyclick.chroniquesdetyrieback.media.delivery.controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyclick.chroniquesdetyrieback.media.delivery.model.LoadedPublicMedia;
import com.tyclick.chroniquesdetyrieback.media.delivery.service.PublicMediaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class PublicMediaController {

    private final PublicMediaService publicMediaService;

    @GetMapping("/{mediaId}")
    public ResponseEntity<Resource> getPublicMedia(
            @PathVariable UUID mediaId
    ) {
        LoadedPublicMedia media =
                publicMediaService.loadPublicMedia(mediaId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.mimeType()))
                .contentLength(media.sizeBytes())
                .cacheControl(
                        CacheControl
                                .maxAge(Duration.ofDays(365))
                                .cachePublic()
                                .immutable()
                )
                .body(media.resource());
    }
}