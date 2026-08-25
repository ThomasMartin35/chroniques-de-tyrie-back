package com.tyclick.chroniquesdetyrieback.media.delivery.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tyclick.chroniquesdetyrieback.auth.jwt.JwtService;
import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetailsService;
import com.tyclick.chroniquesdetyrieback.common.config.SecurityConfig;
import com.tyclick.chroniquesdetyrieback.media.delivery.exception.PublicMediaNotFoundException;
import com.tyclick.chroniquesdetyrieback.media.delivery.model.LoadedPublicMedia;
import com.tyclick.chroniquesdetyrieback.media.delivery.service.PublicMediaService;

@WebMvcTest(PublicMediaController.class)
@Import(SecurityConfig.class)
class PublicMediaControllerTest {

    private static final byte[] IMAGE_CONTENT = {1, 2, 3, 4};

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicMediaService publicMediaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnPublicAvatarWithoutAuthentication() throws Exception {
        UUID mediaId = UUID.randomUUID();
        LoadedPublicMedia media = new LoadedPublicMedia(
                new ByteArrayResource(IMAGE_CONTENT),
                "image/webp",
                IMAGE_CONTENT.length
        );

        when(publicMediaService.loadPublicMedia(mediaId))
                .thenReturn(media);

        mockMvc.perform(get("/api/media/{mediaId}", mediaId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/webp"))
                .andExpect(content().bytes(IMAGE_CONTENT))
                .andExpect(header().longValue(
                        HttpHeaders.CONTENT_LENGTH,
                        IMAGE_CONTENT.length
                ))
                .andExpect(header().string(
                        HttpHeaders.CACHE_CONTROL,
                        containsString("max-age=31536000")
                ))
                .andExpect(header().string(
                        HttpHeaders.CACHE_CONTROL,
                        containsString("public")
                ))
                .andExpect(header().string(
                        HttpHeaders.CACHE_CONTROL,
                        containsString("immutable")
                ));

        verify(publicMediaService).loadPublicMedia(mediaId);
    }

    @Test
    void shouldReturnNotFoundWhenPublicMediaDoesNotExist() throws Exception {
        UUID mediaId = UUID.randomUUID();

        when(publicMediaService.loadPublicMedia(mediaId))
                .thenThrow(new PublicMediaNotFoundException());

        mockMvc.perform(get("/api/media/{mediaId}", mediaId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        "Public media not found"
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/media/" + mediaId
                ));
    }

    @Test
    void shouldReturnBadRequestForInvalidMediaIdentifier() throws Exception {
        mockMvc.perform(get("/api/media/not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(publicMediaService);
    }

    @Test
    void shouldKeepNonGetMediaRoutesProtected() throws Exception {
        UUID mediaId = UUID.randomUUID();

        mockMvc.perform(delete("/api/media/{mediaId}", mediaId))
                .andExpect(status().isForbidden());

        verifyNoInteractions(publicMediaService);
    }
}
