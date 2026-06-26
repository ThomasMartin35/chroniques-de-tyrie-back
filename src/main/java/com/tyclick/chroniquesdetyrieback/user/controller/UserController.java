package com.tyclick.chroniquesdetyrieback.user.controller;

import com.tyclick.chroniquesdetyrieback.user.dto.request.UpdateProfileRequest;
import com.tyclick.chroniquesdetyrieback.user.dto.response.UserProfileResponse;
import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import com.tyclick.chroniquesdetyrieback.user.mapper.UserMapper;
import com.tyclick.chroniquesdetyrieback.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;

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
}
