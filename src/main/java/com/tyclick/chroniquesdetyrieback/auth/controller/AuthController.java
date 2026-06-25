package com.tyclick.chroniquesdetyrieback.auth.controller;

import com.tyclick.chroniquesdetyrieback.auth.dto.request.LoginRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.request.RegisterRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.CurrentUserResponse;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.LoginResponse;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.RegisterResponse;
import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import com.tyclick.chroniquesdetyrieback.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    // Endpoint to handle user registration
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login (@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return CurrentUserResponse.builder()
                .id(customUserDetails.getUser().getId())
                .username(customUserDetails.getUser().getUsername())
                .email(customUserDetails.getUser().getEmail())
                .role(customUserDetails.getUser().getRole().name())
                .build();
    }
}
