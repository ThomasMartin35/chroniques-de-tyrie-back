package com.tyclick.chroniquesdetyrieback.media.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import com.tyclick.chroniquesdetyrieback.media.delivery.exception.PublicMediaNotFoundException;
import com.tyclick.chroniquesdetyrieback.media.delivery.model.LoadedPublicMedia;
import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import com.tyclick.chroniquesdetyrieback.media.entity.MediaPurpose;
import com.tyclick.chroniquesdetyrieback.media.repository.MediaRepository;
import com.tyclick.chroniquesdetyrieback.media.storage.MediaStorage;
import com.tyclick.chroniquesdetyrieback.media.storage.exception.MediaFileNotFoundException;
import com.tyclick.chroniquesdetyrieback.media.storage.exception.MediaStorageException;

@ExtendWith(MockitoExtension.class)
class PublicMediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaStorage mediaStorage;

    private PublicMediaService publicMediaService;

    @BeforeEach
    void setUp() {
        publicMediaService = new PublicMediaService(
                mediaRepository,
                mediaStorage
        );
    }

    @Test
    void shouldLoadExistingPublicAvatar() {
        UUID mediaId = UUID.randomUUID();
        Media media = createAvatar(mediaId);
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});

        when(mediaRepository.findById(mediaId))
                .thenReturn(Optional.of(media));
        when(mediaStorage.load(media.getStorageKey()))
                .thenReturn(resource);

        LoadedPublicMedia result =
                publicMediaService.loadPublicMedia(mediaId);

        assertSame(resource, result.resource());
        assertEquals("image/webp", result.mimeType());
        assertEquals(3L, result.sizeBytes());
        verify(mediaStorage).load(media.getStorageKey());
    }

    @Test
    void shouldRejectUnknownMediaIdentifier() {
        UUID mediaId = UUID.randomUUID();

        when(mediaRepository.findById(mediaId))
                .thenReturn(Optional.empty());

        assertThrows(
                PublicMediaNotFoundException.class,
                () -> publicMediaService.loadPublicMedia(mediaId)
        );

        verifyNoInteractions(mediaStorage);
    }

    @Test
    void shouldRejectMediaThatIsNotExplicitlyPublic() {
        UUID mediaId = UUID.randomUUID();
        Media media = createAvatar(mediaId);
        media.setPurpose(null);

        when(mediaRepository.findById(mediaId))
                .thenReturn(Optional.of(media));

        assertThrows(
                PublicMediaNotFoundException.class,
                () -> publicMediaService.loadPublicMedia(mediaId)
        );

        verifyNoInteractions(mediaStorage);
    }

    @Test
    void shouldHideMissingPhysicalFileBehindNotFoundException() {
        UUID mediaId = UUID.randomUUID();
        Media media = createAvatar(mediaId);

        when(mediaRepository.findById(mediaId))
                .thenReturn(Optional.of(media));
        when(mediaStorage.load(media.getStorageKey()))
                .thenThrow(new MediaFileNotFoundException());

        assertThrows(
                PublicMediaNotFoundException.class,
                () -> publicMediaService.loadPublicMedia(mediaId)
        );
    }

    @Test
    void shouldPropagateTechnicalStorageFailure() {
        UUID mediaId = UUID.randomUUID();
        Media media = createAvatar(mediaId);
        MediaStorageException storageException =
                new MediaStorageException("Storage unavailable");

        when(mediaRepository.findById(mediaId))
                .thenReturn(Optional.of(media));
        when(mediaStorage.load(media.getStorageKey()))
                .thenThrow(storageException);

        MediaStorageException result = assertThrows(
                MediaStorageException.class,
                () -> publicMediaService.loadPublicMedia(mediaId)
        );

        assertSame(storageException, result);
    }

    private Media createAvatar(UUID mediaId) {
        return Media.builder()
                .id(mediaId)
                .storageKey("avatars/" + mediaId + ".webp")
                .mimeType("image/webp")
                .sizeBytes(3L)
                .purpose(MediaPurpose.AVATAR)
                .build();
    }
}
