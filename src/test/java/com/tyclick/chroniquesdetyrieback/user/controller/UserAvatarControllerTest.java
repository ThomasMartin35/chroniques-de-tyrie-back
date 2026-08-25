package com.tyclick.chroniquesdetyrieback.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetailsService;
import com.tyclick.chroniquesdetyrieback.auth.jwt.JwtService;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.AvatarFileTooLargeException;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.InvalidAvatarImageException;
import com.tyclick.chroniquesdetyrieback.media.avatar.exception.UnsupportedAvatarFormatException;
import com.tyclick.chroniquesdetyrieback.media.avatar.service.UserAvatarService;
import com.tyclick.chroniquesdetyrieback.media.delivery.url.PublicMediaUrlBuilder;
import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.entity.UserRole;
import com.tyclick.chroniquesdetyrieback.user.mapper.UserMapper;
import com.tyclick.chroniquesdetyrieback.user.service.UserService;

@WebMvcTest(UserController.class)
class UserAvatarControllerTest {

    private static final String ENDPOINT = "/api/users/me/avatar";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserAvatarService userAvatarService;

    @MockitoBean
    private PublicMediaUrlBuilder publicMediaUrlBuilder;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldUploadAvatarForAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        MockMultipartFile file = createFile();
        Media savedAvatar = Media.builder()
                .id(mediaId)
                .build();

        when(userAvatarService.uploadOrReplaceAvatar(
                eq(userId),
                any(MultipartFile.class)
        )).thenReturn(savedAvatar);
        when(publicMediaUrlBuilder.build(savedAvatar))
                .thenReturn("http://localhost/api/media/" + mediaId);

        mockMvc.perform(multipart(HttpMethod.PUT, ENDPOINT)
                        .file(file)
                        .with(authentication(authenticationFor(userId)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(mediaId.toString()))
                .andExpect(jsonPath("$.avatarUrl").value(
                        "http://localhost/api/media/" + mediaId
                ));

        verify(userAvatarService).uploadOrReplaceAvatar(
                eq(userId),
                any(MultipartFile.class)
        );
    }

    @Test
    void shouldRejectRequestWithoutFile() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(multipart(HttpMethod.PUT, ENDPOINT)
                        .with(authentication(authenticationFor(userId)))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userAvatarService);
    }

    @Test
    void shouldReturnBadRequestForInvalidImage() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userAvatarService.uploadOrReplaceAvatar(
                eq(userId),
                any(MultipartFile.class)
        )).thenThrow(new InvalidAvatarImageException());

        mockMvc.perform(authenticatedUpload(userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Avatar file does not contain a valid image"
                ))
                .andExpect(jsonPath("$.path").value(ENDPOINT));
    }

    @Test
    void shouldReturnPayloadTooLargeForOversizedAvatar() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userAvatarService.uploadOrReplaceAvatar(
                eq(userId),
                any(MultipartFile.class)
        )).thenThrow(new AvatarFileTooLargeException());

        mockMvc.perform(authenticatedUpload(userId))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.message").value(
                        "Avatar file exceeds the maximum allowed size"
                ))
                .andExpect(jsonPath("$.path").value(ENDPOINT));
    }

    @Test
    void shouldReturnUnsupportedMediaTypeForUnsupportedAvatarFormat()
            throws Exception {
        UUID userId = UUID.randomUUID();

        when(userAvatarService.uploadOrReplaceAvatar(
                eq(userId),
                any(MultipartFile.class)
        )).thenThrow(new UnsupportedAvatarFormatException());

        mockMvc.perform(authenticatedUpload(userId))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.message").value(
                        "Avatar image format must be JPEG, PNG, or WebP"
                ))
                .andExpect(jsonPath("$.path").value(ENDPOINT));
    }

    @Test
    void shouldRejectUnauthenticatedUpload() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PUT, ENDPOINT)
                        .file(createFile())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userAvatarService);
    }

    private org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
    authenticatedUpload(UUID userId) {
        return multipart(HttpMethod.PUT, ENDPOINT)
                .file(createFile())
                .with(authentication(authenticationFor(userId)))
                .with(csrf());
    }

    private Authentication authenticationFor(UUID userId) {
        User user = User.builder()
                .id(userId)
                .email("member@test.fr")
                .passwordHash("encoded-password")
                .role(UserRole.ROLE_MEMBER)
                .isActive(true)
                .build();
        CustomUserDetails principal = new CustomUserDetails(user);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(UserRole.ROLE_MEMBER.name()))
        );
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );
    }
}
