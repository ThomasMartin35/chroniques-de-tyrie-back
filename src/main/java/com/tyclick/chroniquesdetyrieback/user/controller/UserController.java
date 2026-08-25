package com.tyclick.chroniquesdetyrieback.user.controller;

import com.tyclick.chroniquesdetyrieback.common.dto.response.MessageResponse;
import com.tyclick.chroniquesdetyrieback.media.avatar.dto.response.AvatarResponse;
import com.tyclick.chroniquesdetyrieback.media.avatar.service.UserAvatarService;
import com.tyclick.chroniquesdetyrieback.media.delivery.url.PublicMediaUrlBuilder;
import com.tyclick.chroniquesdetyrieback.media.entity.Media;
import com.tyclick.chroniquesdetyrieback.user.dto.request.ChangePasswordRequest;
import com.tyclick.chroniquesdetyrieback.user.dto.request.UpdateProfileRequest;
import com.tyclick.chroniquesdetyrieback.user.dto.response.UserProfileResponse;
import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import com.tyclick.chroniquesdetyrieback.user.mapper.UserMapper;
import com.tyclick.chroniquesdetyrieback.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final UserAvatarService userAvatarService;
    private final PublicMediaUrlBuilder publicMediaUrlBuilder;

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return userMapper.toUserProfileResponse(customUserDetails.getUser());
    }

    @PatchMapping("/me")
    public UserProfileResponse updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequest updateProfileRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userService.updateCurrentUserProfile(updateProfileRequest, userDetails.getUser().getId());
    }

    @PatchMapping("/me/password")
    public MessageResponse changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userService.changePassword(
                userDetails.getUser().getId(),
                request
        );
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AvatarResponse uploadOrReplaceAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Media avatar = userAvatarService.uploadOrReplaceAvatar(userDetails.getUser().getId(), file);
        String avatarUrl = publicMediaUrlBuilder.build(avatar);

        return new AvatarResponse(avatar.getId(), avatarUrl);
    }
}
