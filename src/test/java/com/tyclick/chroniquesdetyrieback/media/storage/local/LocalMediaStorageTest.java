package com.tyclick.chroniquesdetyrieback.media.storage.local;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import com.tyclick.chroniquesdetyrieback.media.storage.config.MediaStorageProperties;
import com.tyclick.chroniquesdetyrieback.media.storage.exception.MediaStorageException;

class LocalMediaStorageTest {

    @TempDir
    Path temporaryDirectory;

    private Path storageRoot;
    private LocalMediaStorage localMediaStorage;

    @BeforeEach
    void setUp() {
        storageRoot = temporaryDirectory.resolve("media-storage");
        localMediaStorage = new LocalMediaStorage(
                new MediaStorageProperties(storageRoot)
        );
    }

    @Test
    void shouldInitializeRootDirectory() {
        assertTrue(Files.isDirectory(storageRoot));
    }

    @Test
    void shouldStoreMediaFile() throws IOException {
        byte[] content = mediaContent();

        String storageKey = localMediaStorage.store(
                content,
                "avatars",
                "WEBP"
        );

        Path storedFile = storageRoot.resolve(storageKey);

        assertTrue(storageKey.startsWith("avatars/"));
        assertTrue(storageKey.endsWith(".webp"));
        assertTrue(Files.isRegularFile(storedFile));
        assertArrayEquals(content, Files.readAllBytes(storedFile));
    }

    @Test
    void shouldLoadStoredMediaFile() throws IOException {
        byte[] content = mediaContent();
        String storageKey = localMediaStorage.store(
                content,
                "avatars",
                "webp"
        );

        Resource resource = localMediaStorage.load(storageKey);

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());

        try (InputStream inputStream = resource.getInputStream()) {
            assertArrayEquals(content, inputStream.readAllBytes());
        }
    }

    @Test
    void shouldDeleteStoredMediaFile() {
        String storageKey = localMediaStorage.store(
                mediaContent(),
                "avatars",
                "webp"
        );
        Path storedFile = storageRoot.resolve(storageKey);

        localMediaStorage.delete(storageKey);

        assertFalse(Files.exists(storedFile));
    }

    @Test
    void shouldIgnoreDeletionWhenMediaFileDoesNotExist() {
        assertDoesNotThrow(
                () -> localMediaStorage.delete("avatars/missing.webp")
        );
    }

    @Test
    void shouldRejectEmptyMediaContent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> localMediaStorage.store(
                        new byte[0],
                        "avatars",
                        "webp"
                )
        );
    }

    @Test
    void shouldRejectBlankDirectory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> localMediaStorage.store(
                        mediaContent(),
                        " ",
                        "webp"
                )
        );
    }

    @Test
    void shouldRejectBlankExtension() {
        assertThrows(
                IllegalArgumentException.class,
                () -> localMediaStorage.store(
                        mediaContent(),
                        "avatars",
                        " "
                )
        );
    }

    @Test
    void shouldRejectInvalidExtension() {
        assertThrows(
                IllegalArgumentException.class,
                () -> localMediaStorage.store(
                        mediaContent(),
                        "avatars",
                        "web/p"
                )
        );
    }

    @Test
    void shouldRejectLoadingMissingMediaFile() {
        assertThrows(
                MediaStorageException.class,
                () -> localMediaStorage.load("avatars/missing.webp")
        );
    }

    @Test
    void shouldRejectDirectoryTraversalWhenStoring() {
        assertThrows(
                MediaStorageException.class,
                () -> localMediaStorage.store(
                        mediaContent(),
                        "../outside",
                        "webp"
                )
        );
    }

    @Test
    void shouldRejectDirectoryTraversalWhenLoading() {
        assertThrows(
                MediaStorageException.class,
                () -> localMediaStorage.load("../outside.webp")
        );
    }

    @Test
    void shouldRejectDirectoryTraversalWhenDeleting() {
        assertThrows(
                MediaStorageException.class,
                () -> localMediaStorage.delete("../outside.webp")
        );
    }

    private byte[] mediaContent() {
        return "fake image content".getBytes(StandardCharsets.UTF_8);
    }
}
