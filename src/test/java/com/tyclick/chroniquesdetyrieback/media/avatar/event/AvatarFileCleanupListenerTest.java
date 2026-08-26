package com.tyclick.chroniquesdetyrieback.media.avatar.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tyclick.chroniquesdetyrieback.media.storage.MediaStorage;
import com.tyclick.chroniquesdetyrieback.media.storage.exception.MediaStorageException;

@ExtendWith(MockitoExtension.class)
class AvatarFileCleanupListenerTest {

    @Mock
    private MediaStorage mediaStorage;

    private AvatarFileCleanupListener listener;

    @BeforeEach
    void setUp() {
        listener = new AvatarFileCleanupListener(mediaStorage);
    }

    @Test
    void shouldDeletePreviousFileAfterSuccessfulReplacement() {
        AvatarReplacementEvent event = new AvatarReplacementEvent(
                "avatars/new.webp",
                "avatars/previous.webp"
        );

        listener.handleSuccessfulReplacement(event);

        verify(mediaStorage).delete("avatars/previous.webp");
        verify(mediaStorage, never()).delete("avatars/new.webp");
    }

    @Test
    void shouldDeleteNewFileAfterFailedReplacement() {
        AvatarReplacementEvent event = new AvatarReplacementEvent(
                "avatars/new.webp",
                "avatars/previous.webp"
        );

        listener.handleFailedReplacement(event);

        verify(mediaStorage).delete("avatars/new.webp");
        verify(mediaStorage, never()).delete("avatars/previous.webp");
    }

    @Test
    void shouldIgnoreMissingPreviousFileAfterInitialUpload() {
        AvatarReplacementEvent event = new AvatarReplacementEvent(
                "avatars/new.webp",
                null
        );

        listener.handleSuccessfulReplacement(event);

        verify(mediaStorage, never()).delete(null);
    }

    @Test
    void shouldNotPropagateCleanupFailureAfterTransactionCompletion() {
        AvatarReplacementEvent event = new AvatarReplacementEvent(
                "avatars/new.webp",
                "avatars/previous.webp"
        );
        doThrow(new MediaStorageException("Deletion failed"))
                .when(mediaStorage)
                .delete("avatars/previous.webp");

        assertDoesNotThrow(
                () -> listener.handleSuccessfulReplacement(event)
        );
    }

    @Test
    void shouldDeleteAvatarFileAfterSuccessfulDeletion() {
        AvatarDeletionEvent event = new AvatarDeletionEvent(
                "avatars/deleted.webp"
        );

        listener.handleSuccessfulDeletion(event);

        verify(mediaStorage).delete("avatars/deleted.webp");
    }

    @Test
    void shouldNotPropagateDeletionCleanupFailure() {
        AvatarDeletionEvent event = new AvatarDeletionEvent(
                "avatars/deleted.webp"
        );
        doThrow(new MediaStorageException("Deletion failed"))
                .when(mediaStorage)
                .delete("avatars/deleted.webp");

        assertDoesNotThrow(
                () -> listener.handleSuccessfulDeletion(event)
        );
    }
}
