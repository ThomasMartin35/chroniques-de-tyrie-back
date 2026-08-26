package com.tyclick.chroniquesdetyrieback.media.avatar.event;

import com.tyclick.chroniquesdetyrieback.media.storage.MediaStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvatarFileCleanupListener {

    private final MediaStorage mediaStorage;

    /**
     * Handles the cleanup of the previous avatar file after a successful replacement.
     *
     * @param event the event containing the storage keys of the new and previous avatar files
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSuccessfulReplacement(AvatarReplacementEvent event) {
        deleteSafely(
                event.previousStorageKey(),
                "previous"
        );
    }

    /**
     * Handles the cleanup of the new avatar file after a failed replacement.
     *
     * @param event the event containing the storage keys of the new and previous avatar files
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleFailedReplacement(AvatarReplacementEvent event) {
        deleteSafely(
                event.newStorageKey(),
                "new"
        );
    }

    /**
     * Deletes the file with the given storage key safely, logging any exceptions that occur during deletion.
     *
     * @param storageKey the storage key of the file to delete
     * @param fileRole   the role of the file (e.g., "previous" or "new") for logging purposes
     */
    private void deleteSafely(String storageKey, String fileRole) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }

        try {
            mediaStorage.delete(storageKey);
        } catch (RuntimeException exception) {
            log.error(
                    "Failed to delete {} avatar file with storage key {}",
                    fileRole,
                    storageKey,
                    exception
            );
        }
    }

    /**
     * Deletes the avatar file after its database deletion has been committed.
     *
     * @param event the event containing the deleted avatar storage key
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSuccessfulDeletion(AvatarDeletionEvent event) {
        deleteSafely(
                event.storageKey(),
                "deleted"
        );
    }
}