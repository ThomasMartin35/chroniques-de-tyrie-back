package com.tyclick.chroniquesdetyrieback.auth.passwordreset.dto.request;

import com.tyclick.chroniquesdetyrieback.common.validation.password.ValidPassword;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(

        @NotBlank(message = "Token is required")
        String token,

        @NotBlank(message = "Password is required")
        @ValidPassword
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword

) {
}