package com.tyclick.chroniquesdetyrieback.auth.passwordreset.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tyclick.chroniquesdetyrieback.auth.passwordreset.dto.request.ForgotPasswordRequest;
import com.tyclick.chroniquesdetyrieback.auth.passwordreset.dto.response.ForgotPasswordResponse;
import com.tyclick.chroniquesdetyrieback.auth.passwordreset.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ForgotPasswordResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.requestPasswordReset(request.email());

        return new ForgotPasswordResponse(
                "If an account matches this email address, "
                + "a password reset link has been sent."
        );
    }
}
