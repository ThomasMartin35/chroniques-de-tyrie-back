package com.tyclick.chroniquesdetyrieback.user.controller;

import com.tyclick.chroniquesdetyrieback.auth.dto.response.UserProfileResponse;
import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import com.tyclick.chroniquesdetyrieback.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserProfileResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return userMapper.toUserProfileResponse(customUserDetails.getUser());
    }
}
