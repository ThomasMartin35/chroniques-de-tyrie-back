package com.tyclick.chroniquesdetyrieback.media.avatar.processing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tyclick.chroniquesdetyrieback.media.avatar.config.AvatarImageProperties;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.AvatarFileTooLargeException;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.AvatarImageProcessingException;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.InvalidAvatarImageException;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.UnsupportedAvatarFormatException;

import net.coobird.thumbnailator.Thumbnails;

@Component
public class AvatarImageProcessor {

    private static final String OUTPUT_MIME_TYPE = "image/webp";
    private static final String OUTPUT_EXTENSION = "webp";

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> SUPPORTED_FORMAT_NAMES = Set.of(
            "jpeg",
            "png",
            "webp"
    );

    private final AvatarImageProperties properties;

    public AvatarImageProcessor(AvatarImageProperties properties) {
        this.properties = properties;
    }

    /**
     * Processes the given avatar image file by validating, resizing (if necessary), and converting it to WebP format.
     * @param file the avatar image file to process
     * @return a {@link ProcessedAvatarImage} containing the processed image data and metadata
     */
    public ProcessedAvatarImage process(MultipartFile file) {
        validateFile(file);

        BufferedImage image = decodeAndValidateImage(file);

        try {
            BufferedImage resizedImage = resizeIfNecessary(image);
            byte[] content = encodeToWebp(resizedImage);

            return new ProcessedAvatarImage(
                    content,
                    OUTPUT_MIME_TYPE,
                    OUTPUT_EXTENSION,
                    resizedImage.getWidth(),
                    resizedImage.getHeight()
            );
        } catch (IOException exception) {
            throw new AvatarImageProcessingException(exception);
        }
    }

    /**
     * Validates the given avatar image file for size and format constraints.
     * @param file the avatar image file to validate
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAvatarImageException(
                    "Avatar file must not be empty"
            );
        }

        if (file.getSize() > properties.maxFileSize().toBytes()) {
            throw new AvatarFileTooLargeException();
        }

        if (!SUPPORTED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new UnsupportedAvatarFormatException();
        }
    }

    /**
     * Decodes the given avatar image file into a {@link BufferedImage} and validates its format and pixel count.
     * @param file the avatar image file to decode and validate
     * @return the decoded {@link BufferedImage}
     */
    private BufferedImage decodeAndValidateImage(MultipartFile file) {
        try (ImageInputStream inputStream =
                     ImageIO.createImageInputStream(file.getInputStream())) {

            if (inputStream == null) {
                throw new InvalidAvatarImageException();
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(inputStream);

            if (!readers.hasNext()) {
                throw new InvalidAvatarImageException();
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(inputStream, true, true);

                String formatName = reader
                        .getFormatName()
                        .toLowerCase(Locale.ROOT);

                if (!SUPPORTED_FORMAT_NAMES.contains(formatName)) {
                    throw new UnsupportedAvatarFormatException();
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                long pixelCount = (long) width * height;

                if (pixelCount > properties.maxPixelCount()) {
                    throw new InvalidAvatarImageException(
                            "Avatar image exceeds the maximum allowed pixel count"
                    );
                }

                BufferedImage image = reader.read(0);

                if (image == null) {
                    throw new InvalidAvatarImageException();
                }

                return image;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new InvalidAvatarImageException();
        }
    }

    /**
     * Resizes the given source image if its dimensions exceed the maximum allowed width or height.
     * @param sourceImage the source {@link BufferedImage} to resize if necessary
     * @return the resized {@link BufferedImage} if resizing was performed, or the original image if no resizing was needed
     * @throws IOException if an error occurs during the resizing process
     */
    private BufferedImage resizeIfNecessary(BufferedImage sourceImage)
            throws IOException {
        if (sourceImage.getWidth() <= properties.maxWidth()
                && sourceImage.getHeight() <= properties.maxHeight()) {
            return sourceImage;
        }

        return Thumbnails.of(sourceImage)
                .size(properties.maxWidth(), properties.maxHeight())
                .keepAspectRatio(true)
                .asBufferedImage();
    }

    /**
     * Encodes the given {@link BufferedImage} to WebP format with the specified quality.
     * @param image the {@link BufferedImage} to encode to WebP format
     * @return a byte array containing the encoded WebP image data
     * @throws IOException if an error occurs during the encoding process
     */
    private byte[] encodeToWebp(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                    .scale(1.0)
                    .outputFormat(OUTPUT_EXTENSION)
                    .outputQuality(properties.webpQuality())
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }
}
