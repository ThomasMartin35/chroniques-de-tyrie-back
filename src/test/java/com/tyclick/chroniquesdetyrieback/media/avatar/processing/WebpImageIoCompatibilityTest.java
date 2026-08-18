package com.tyclick.chroniquesdetyrieback.media.avatar.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class WebpImageIoCompatibilityTest {

    @Test
    void shouldEncodeAndDecodeWebpImage() throws IOException {
        BufferedImage originalImage = new BufferedImage(
                2,
                2,
                BufferedImage.TYPE_INT_RGB
        );

        originalImage.setRGB(0, 0, Color.RED.getRGB());
        originalImage.setRGB(1, 0, Color.GREEN.getRGB());
        originalImage.setRGB(0, 1, Color.BLUE.getRGB());
        originalImage.setRGB(1, 1, Color.WHITE.getRGB());

        byte[] encodedImage;

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            boolean encoded = ImageIO.write(
                    originalImage,
                    "webp",
                    outputStream
            );

            assertTrue(
                    encoded,
                    "No WebP ImageIO writer is available"
            );

            encodedImage = outputStream.toByteArray();
        }

        assertTrue(encodedImage.length > 0);

        BufferedImage decodedImage;

        try (ByteArrayInputStream inputStream =
                     new ByteArrayInputStream(encodedImage)) {

            decodedImage = ImageIO.read(inputStream);
        }

        assertNotNull(
                decodedImage,
                "The encoded WebP image could not be decoded"
        );
        assertEquals(2, decodedImage.getWidth());
        assertEquals(2, decodedImage.getHeight());
    }
}