package com.tyclick.chroniquesdetyrieback.media.storage.local;

import com.tyclick.chroniquesdetyrieback.media.storage.MediaStorage;
import com.tyclick.chroniquesdetyrieback.media.storage.config.MediaStorageProperties;
import com.tyclick.chroniquesdetyrieback.media.storage.exception.MediaStorageException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Component
public class LocalMediaStorage implements MediaStorage {

    private final Path rootDirectory;

    public LocalMediaStorage(MediaStorageProperties properties) {
        this.rootDirectory = properties.rootDirectory()
                .toAbsolutePath()
                .normalize();

        initializeRootDirectory();
    }

    private void initializeRootDirectory() {
        try {
            Files.createDirectories(rootDirectory);
        } catch (IOException exception) {
            throw new MediaStorageException(
                    "Failed to initialize media storage directory",
                    exception
            );
        }
    }

    @Override
    public String store(byte[] content, String directory, String extension) {
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Media content must not be empty");
        }

        if (directory == null || directory.isBlank()) {
            throw new IllegalArgumentException("Media directory must not be blank");
        }

        String normalizedExtension = normalizeExtension(extension);
        String filename = UUID.randomUUID() + "." + normalizedExtension;

        try {
            Path relativePath = Path.of(directory)
                    .resolve(filename)
                    .normalize();

            Path targetPath = resolveSafely(relativePath);

            Files.createDirectories(targetPath.getParent());
            Files.write(
                    targetPath,
                    content,
                    StandardOpenOption.CREATE_NEW
            );

            return relativePath
                    .toString()
                    .replace('\\', '/');
        } catch (InvalidPathException exception) {
            throw new MediaStorageException(
                    "Invalid media storage path",
                    exception
            );
        } catch (IOException exception) {
            throw new MediaStorageException(
                    "Failed to store media file",
                    exception
            );
        }
    }

    private String normalizeExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new IllegalArgumentException(
                    "Media extension must not be blank"
            );
        }

        String normalizedExtension = extension.startsWith(".")
                ? extension.substring(1)
                : extension;

        if (!normalizedExtension.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException(
                    "Media extension contains invalid characters"
            );
        }

        return normalizedExtension.toLowerCase(Locale.ROOT);
    }

    private Path resolveSafely(Path relativePath) {
        if (relativePath.isAbsolute()) {
            throw new MediaStorageException(
                    "Absolute media paths are not allowed"
            );
        }

        Path resolvedPath = rootDirectory
                .resolve(relativePath)
                .normalize();

        if (!resolvedPath.startsWith(rootDirectory)) {
            throw new MediaStorageException(
                    "Media path escapes the storage directory"
            );
        }

        return resolvedPath;
    }

    @Override
    public Resource load(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Media storage key must not be blank"
            );
        }

        try {
            Path relativePath = Path.of(storageKey).normalize();
            Path targetPath = resolveSafely(relativePath);

            if (!Files.isRegularFile(targetPath)
                    || !Files.isReadable(targetPath)) {
                throw new MediaStorageException(
                        "Media file does not exist or is not readable"
                );
            }

            return new UrlResource(targetPath.toUri());
        } catch (InvalidPathException exception) {
            throw new MediaStorageException(
                    "Invalid media storage path",
                    exception
            );
        } catch (MalformedURLException exception) {
            throw new MediaStorageException(
                    "Failed to load media file",
                    exception
            );
        }
    }

    @Override
    public void delete(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Media storage key must not be blank"
            );
        }

        try {
            Path relativePath = Path.of(storageKey).normalize();
            Path targetPath = resolveSafely(relativePath);

            if (Files.exists(targetPath)
                    && !Files.isRegularFile(targetPath)) {
                throw new MediaStorageException(
                        "Media storage key does not reference a regular file"
                );
            }

            Files.deleteIfExists(targetPath);
        } catch (InvalidPathException exception) {
            throw new MediaStorageException(
                    "Invalid media storage path",
                    exception
            );
        } catch (IOException exception) {
            throw new MediaStorageException(
                    "Failed to delete media file",
                    exception
            );
        }
    }
}