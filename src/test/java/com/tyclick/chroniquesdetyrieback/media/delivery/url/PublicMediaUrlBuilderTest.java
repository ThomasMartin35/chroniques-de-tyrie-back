package com.tyclick.chroniquesdetyrieback.media.delivery.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.tyclick.chroniquesdetyrieback.media.entity.Media;

class PublicMediaUrlBuilderTest {

    private PublicMediaUrlBuilder publicMediaUrlBuilder;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request)
        );

        publicMediaUrlBuilder = new PublicMediaUrlBuilder();
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldBuildPublicUrlFromMediaIdentifier() {
        UUID mediaId = UUID.randomUUID();

        String result = publicMediaUrlBuilder.build(mediaId);

        assertEquals(
                "http://localhost:8080/api/media/" + mediaId,
                result
        );
    }

    @Test
    void shouldBuildPublicUrlFromMedia() {
        UUID mediaId = UUID.randomUUID();
        Media media = Media.builder()
                .id(mediaId)
                .build();

        String result = publicMediaUrlBuilder.build(media);

        assertEquals(
                "http://localhost:8080/api/media/" + mediaId,
                result
        );
    }

    @Test
    void shouldReturnNullWhenMediaOrIdentifierIsMissing() {
        assertNull(publicMediaUrlBuilder.build((UUID) null));
        assertNull(publicMediaUrlBuilder.build((Media) null));
    }
}
