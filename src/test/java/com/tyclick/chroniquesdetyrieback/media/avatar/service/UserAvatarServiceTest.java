package com.tyclick.chroniquesdetyrieback.media.avatar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.media.avatar.event.AvatarReplacementEvent;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.InvalidAvatarImageException;
import com.tyclick.chroniquesdetyrieback.media.avatar.processing.AvatarImageProcessor;
import com.tyclick.chroniquesdetyrieback.media.avatar.processing.ProcessedAvatarImage;
import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import com.tyclick.chroniquesdetyrieback.media.entity.MediaPurpose;
import com.tyclick.chroniquesdetyrieback.media.repository.MediaRepository;
import com.tyclick.chroniquesdetyrieback.media.storage.MediaStorage;
import com.tyclick.chroniquesdetyrieback.media.storage.exception.MediaStorageException;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserAvatarServiceTest {

    private static final byte[] PROCESSED_CONTENT = {1, 2, 3};
    private static final String NEW_STORAGE_KEY = "avatars/new-avatar.webp";

    @Mock
    private AvatarImageProcessor avatarImageProcessor;

    @Mock
    private MediaStorage mediaStorage;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UserAvatarService userAvatarService;

    @BeforeEach
    void setUp() {
        userAvatarService = new UserAvatarService(
                avatarImageProcessor,
                mediaStorage,
                mediaRepository,
                userRepository,
                eventPublisher
        );
    }

    @Test
    void shouldUploadAvatarForUserWithoutPreviousAvatar() {
        User user = User.builder().id(UUID.randomUUID()).build();
        MockMultipartFile file = createFile("portrait.png");
        ProcessedAvatarImage processedImage = createProcessedImage();
        Media savedAvatar = Media.builder()
                .id(UUID.randomUUID())
                .storageKey(NEW_STORAGE_KEY)
                .build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        prepareSuccessfulStorage(file, processedImage);
        when(mediaRepository.save(any(Media.class)))
                .thenReturn(savedAvatar);

        Media result = userAvatarService.uploadOrReplaceAvatar(user.getId(), file);

        assertSame(savedAvatar, result);
        assertSame(savedAvatar, user.getAvatar());

        ArgumentCaptor<Media> mediaCaptor =
                ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(mediaCaptor.capture());

        Media mediaToSave = mediaCaptor.getValue();
        assertEquals(NEW_STORAGE_KEY, mediaToSave.getStorageKey());
        assertEquals("portrait.png", mediaToSave.getOriginalFilename());
        assertEquals("image/webp", mediaToSave.getMimeType());
        assertEquals(3L, mediaToSave.getSizeBytes());
        assertEquals(MediaPurpose.AVATAR, mediaToSave.getPurpose());
        assertSame(user, mediaToSave.getUploadedBy());

        verify(eventPublisher).publishEvent(
                new AvatarReplacementEvent(NEW_STORAGE_KEY, null)
        );
        verify(userRepository).saveAndFlush(user);
        verify(mediaRepository, never()).delete(any(Media.class));
        verify(mediaRepository, never()).flush();
    }

    @Test
    void shouldReplacePreviousAvatarAndPublishBothStorageKeys() {
        Media previousAvatar = Media.builder()
                .id(UUID.randomUUID())
                .storageKey("avatars/previous-avatar.webp")
                .build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .avatar(previousAvatar)
                .build();
        MockMultipartFile file = createFile("new-avatar.png");
        ProcessedAvatarImage processedImage = createProcessedImage();
        Media savedAvatar = Media.builder()
                .id(UUID.randomUUID())
                .storageKey(NEW_STORAGE_KEY)
                .build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        prepareSuccessfulStorage(file, processedImage);
        when(mediaRepository.save(any(Media.class)))
                .thenReturn(savedAvatar);

        Media result = userAvatarService.uploadOrReplaceAvatar(
                user.getId(),
                file
        );

        assertSame(savedAvatar, result);
        assertSame(savedAvatar, user.getAvatar());
        verify(eventPublisher).publishEvent(
                new AvatarReplacementEvent(
                        NEW_STORAGE_KEY,
                        previousAvatar.getStorageKey()
                )
        );
        verify(userRepository).saveAndFlush(user);
        verify(mediaRepository).delete(previousAvatar);
        verify(mediaRepository).flush();
    }

    @Test
    void shouldStopBeforeStorageWhenImageProcessingFails() {
        User user = User.builder().id(UUID.randomUUID()).build();
        MockMultipartFile file = createFile("invalid.png");
        InvalidAvatarImageException exception =
                new InvalidAvatarImageException();

        when(avatarImageProcessor.process(file)).thenThrow(exception);

        InvalidAvatarImageException result = assertThrows(
                InvalidAvatarImageException.class,
                () -> userAvatarService.uploadOrReplaceAvatar(
                        user.getId(),
                        file
                )
        );

        assertSame(exception, result);
        verifyNoInteractions(
                mediaStorage,
                mediaRepository,
                userRepository,
                eventPublisher
        );
    }

    @Test
    void shouldStopBeforeDatabaseChangesWhenStorageFails() {
        User user = User.builder().id(UUID.randomUUID()).build();
        MockMultipartFile file = createFile("avatar.png");
        ProcessedAvatarImage processedImage = createProcessedImage();
        MediaStorageException exception =
                new MediaStorageException("Storage unavailable");

        when(avatarImageProcessor.process(file))
                .thenReturn(processedImage);
        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(mediaStorage.store(
                PROCESSED_CONTENT,
                "avatars",
                "webp"
        )).thenThrow(exception);

        MediaStorageException result = assertThrows(
                MediaStorageException.class,
                () -> userAvatarService.uploadOrReplaceAvatar(
                        user.getId(),
                        file
                )
        );

        assertSame(exception, result);
        verify(userRepository).findById(user.getId());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(mediaRepository, eventPublisher);
    }

    @Test
    void shouldRemoveClientPathFromOriginalFilename() {
        User user = User.builder().id(UUID.randomUUID()).build();
        MockMultipartFile file = createFile(
                "C:\\fakepath\\portrait.png"
        );

        Media mediaToSave = captureMediaToSave(user, file);

        assertEquals("portrait.png", mediaToSave.getOriginalFilename());
    }

    @Test
    void shouldUseDefaultOriginalFilenameWhenMissing() {
        User user = User.builder().id(UUID.randomUUID()).build();
        MockMultipartFile file = createFile(null);

        Media mediaToSave = captureMediaToSave(user, file);

        assertEquals("avatar", mediaToSave.getOriginalFilename());
    }

    @Test
    void shouldLimitOriginalFilenameToDatabaseColumnLength() {
        User user = User.builder().id(UUID.randomUUID()).build();
        MockMultipartFile file = createFile("a".repeat(300) + ".png");

        Media mediaToSave = captureMediaToSave(user, file);

        assertEquals(255, mediaToSave.getOriginalFilename().length());
    }

    @Test
    void shouldRejectUploadWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile file = createFile("avatar.png");
        ProcessedAvatarImage processedImage = createProcessedImage();

        when(avatarImageProcessor.process(file))
                .thenReturn(processedImage);
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userAvatarService.uploadOrReplaceAvatar(userId, file)
        );

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(
                mediaStorage,
                mediaRepository,
                eventPublisher
        );
    }

    private Media captureMediaToSave(User user, MockMultipartFile file) {
        ProcessedAvatarImage processedImage = createProcessedImage();
        Media savedAvatar = Media.builder()
                .id(UUID.randomUUID())
                .storageKey(NEW_STORAGE_KEY)
                .build();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        prepareSuccessfulStorage(file, processedImage);
        when(mediaRepository.save(any(Media.class)))
                .thenReturn(savedAvatar);

        userAvatarService.uploadOrReplaceAvatar(user.getId(), file);

        ArgumentCaptor<Media> mediaCaptor =
                ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(mediaCaptor.capture());
        return mediaCaptor.getValue();
    }

    private void prepareSuccessfulStorage(
            MockMultipartFile file,
            ProcessedAvatarImage processedImage
    ) {
        when(avatarImageProcessor.process(file))
                .thenReturn(processedImage);
        when(mediaStorage.store(
                PROCESSED_CONTENT,
                "avatars",
                "webp"
        )).thenReturn(NEW_STORAGE_KEY);
    }

    private ProcessedAvatarImage createProcessedImage() {
        return new ProcessedAvatarImage(
                PROCESSED_CONTENT,
                "image/webp",
                "webp",
                128,
                128
        );
    }

    private MockMultipartFile createFile(String originalFilename) {
        return new MockMultipartFile(
                "file",
                originalFilename,
                "image/png",
                new byte[]{9}
        );
    }
}
