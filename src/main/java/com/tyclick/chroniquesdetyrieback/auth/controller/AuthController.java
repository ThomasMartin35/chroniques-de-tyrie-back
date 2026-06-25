package com.tyclick.chroniquesdetyrieback.auth.controller;

import com.tyclick.chroniquesdetyrieback.auth.dto.request.RegisterRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.RegisterResponse;
import com.tyclick.chroniquesdetyrieback.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
}
