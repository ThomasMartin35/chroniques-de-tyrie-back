package com.tyclick.chroniquesdetyrieback.media.avatar.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import com.tyclick.chroniquesdetyrieback.media.avatar.config.AvatarImageProperties;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.AvatarFileTooLargeException;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.InvalidAvatarImageException;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.UnsupportedAvatarFormatException;

class AvatarImageProcessorTest {

    private static final DataSize MAX_FILE_SIZE = DataSize.ofMegabytes(5);
    private static final int MAX_WIDTH = 512;
    private static final int MAX_HEIGHT = 512;
    private static final long MAX_PIXEL_COUNT = 40_000_000;
    private static final float WEBP_QUALITY = 0.85F;

    private AvatarImageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = createProcessor(MAX_PIXEL_COUNT);
    }

    @ParameterizedTest
    @CsvSource({
            "jpeg, image/jpeg",
            "png, image/png",
            "webp, image/webp"
    })
    void shouldProcessSupportedImageFormats(
            String inputFormat,
            String contentType
    ) throws IOException {
        MockMultipartFile file = createImageFile(
                inputFormat,
                contentType,
                120,
                80
        );

        ProcessedAvatarImage result = processor.process(file);

        assertEquals("image/webp", result.mimeType());
        assertEquals("webp", result.extension());
        assertEquals(120, result.width());
        assertEquals(80, result.height());
        assertTrue(result.sizeBytes() > 0);

        BufferedImage decodedResult = decode(result.content());

        assertNotNull(decodedResult);
        assertEquals(120, decodedResult.getWidth());
        assertEquals(80, decodedResult.getHeight());
    }

    @Test
    void shouldResizeLargeImageAndPreserveAspectRatio() throws IOException {
        MockMultipartFile file = createImageFile(
                "png",
                "image/png",
                1_024,
                512
        );

        ProcessedAvatarImage result = processor.process(file);

        assertEquals(512, result.width());
        assertEquals(256, result.height());

        BufferedImage decodedResult = decode(result.content());

        assertEquals(512, decodedResult.getWidth());
        assertEquals(256, decodedResult.getHeight());
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[0]
        );

        assertThrows(
                InvalidAvatarImageException.class,
                () -> processor.process(file)
        );
    }

    @Test
    void shouldRejectFileLargerThanConfiguredLimit() {
        byte[] oversizedContent = new byte[
                Math.toIntExact(MAX_FILE_SIZE.toBytes() + 1)
        ];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                oversizedContent
        );

        assertThrows(
                AvatarFileTooLargeException.class,
                () -> processor.process(file)
        );
    }

    @Test
    void shouldRejectUnsupportedDeclaredContentType() throws IOException {
        MockMultipartFile file = createImageFile(
                "png",
                "text/plain",
                100,
                100
        );

        assertThrows(
                UnsupportedAvatarFormatException.class,
                () -> processor.process(file)
        );
    }

    @Test
    void shouldRejectNonImageContentWithSupportedContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake-avatar.jpg",
                "image/jpeg",
                "This is not an image".getBytes()
        );

        assertThrows(
                InvalidAvatarImageException.class,
                () -> processor.process(file)
        );
    }

    @Test
    void shouldRejectImageExceedingMaximumPixelCount() throws IOException {
        AvatarImageProcessor restrictedProcessor = createProcessor(9_999);
        MockMultipartFile file = createImageFile(
                "png",
                "image/png",
                100,
                100
        );

        assertThrows(
                InvalidAvatarImageException.class,
                () -> restrictedProcessor.process(file)
        );
    }

    private AvatarImageProcessor createProcessor(long maxPixelCount) {
        AvatarImageProperties properties = new AvatarImageProperties(
                MAX_FILE_SIZE,
                MAX_WIDTH,
                MAX_HEIGHT,
                maxPixelCount,
                WEBP_QUALITY
        );

        return new AvatarImageProcessor(properties);
    }

    private MockMultipartFile createImageFile(
            String format,
            String contentType,
            int width,
            int height
    ) throws IOException {
        BufferedImage image = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = image.createGraphics();

        try {
            graphics.setColor(Color.ORANGE);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            boolean encoded = ImageIO.write(image, format, outputStream);

            assertTrue(encoded, "No ImageIO writer is available for " + format);

            return new MockMultipartFile(
                    "file",
                    "avatar." + format,
                    contentType,
                    outputStream.toByteArray()
            );
        }
    }

    private BufferedImage decode(byte[] content) throws IOException {
        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(content)) {
            return ImageIO.read(inputStream);
        }
    }
}
