package com.tyclick.chroniquesdetyrieback.media.avatar.service;

import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.media.avatar.event.AvatarReplacementEvent;
import com.tyclick.chroniquesdetyrieback.media.avatar.processing.AvatarImageProcessor;
import com.tyclick.chroniquesdetyrieback.media.avatar.processing.ProcessedAvatarImage;
import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import com.tyclick.chroniquesdetyrieback.media.entity.MediaPurpose;
import com.tyclick.chroniquesdetyrieback.media.repository.MediaRepository;
import com.tyclick.chroniquesdetyrieback.media.storage.MediaStorage;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private static final String DEFAULT_ORIGINAL_FILENAME = "avatar";
    private static final int MAX_ORIGINAL_FILENAME_LENGTH = 255;

    private final AvatarImageProcessor avatarImageProcessor;
    private final MediaStorage mediaStorage;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Uploads or replaces the avatar for the specified user. If the user already has an avatar, it will be replaced with the new one.
     * @param userId the ID of the user whose avatar is being uploaded or replaced
     * @param file the new avatar image file to upload
     * @return the newly uploaded or replaced avatar media entity
     * @throws BusinessException if the user is not found or if there are issues with processing the avatar image
     */
    @Transactional
    public Media uploadOrReplaceAvatar(UUID userId, MultipartFile file) {
        ProcessedAvatarImage processedImage =
                avatarImageProcessor.process(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        Media previousAvatar = user.getAvatar();

        String storageKey = mediaStorage.store(
                processedImage.content(),
                "avatars",
                processedImage.extension()
        );

        eventPublisher.publishEvent(
                new AvatarReplacementEvent(
                        storageKey,
                        previousAvatar != null ? previousAvatar.getStorageKey() : null
                )
        );

        Media newAvatar = Media.builder()
                .storageKey(storageKey)
                .originalFilename(normalizeOriginalFilename(file.getOriginalFilename()))
                .mimeType(processedImage.mimeType())
                .sizeBytes(processedImage.sizeBytes())
                .purpose(MediaPurpose.AVATAR)
                .uploadedBy(user)
                .build();

        Media savedAvatar = mediaRepository.save(newAvatar);

        user.setAvatar(savedAvatar);
        userRepository.saveAndFlush(user);

        if (previousAvatar != null) {
            mediaRepository.delete(previousAvatar);
            mediaRepository.flush();
        }

        return savedAvatar;
    }

    /**
     * Normalizes the original filename by removing path separators, control characters, and trimming whitespace.
     * @param originalFilename the original filename to normalize
     * @return the normalized filename, or a default value if the original filename is null or blank
     */
    private String normalizeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return DEFAULT_ORIGINAL_FILENAME;
        }

        String normalizedFilename = originalFilename
                .replace('\\', '/');

        int lastSeparatorIndex = normalizedFilename.lastIndexOf('/');

        if (lastSeparatorIndex >= 0) {
            normalizedFilename =
                    normalizedFilename.substring(lastSeparatorIndex + 1);
        }

        normalizedFilename = normalizedFilename
                .replaceAll("\\p{Cntrl}", "")
                .trim();

        if (normalizedFilename.isBlank()) {
            return DEFAULT_ORIGINAL_FILENAME;
        }

        if (normalizedFilename.length() > MAX_ORIGINAL_FILENAME_LENGTH) {
            return normalizedFilename.substring(
                    0,
                    MAX_ORIGINAL_FILENAME_LENGTH
            );
        }

        return normalizedFilename;
    }

}
