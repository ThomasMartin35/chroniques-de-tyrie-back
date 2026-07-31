package com.tyclick.chroniquesdetyrieback.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tyclick.chroniquesdetyrieback.auth.refreshtoken.cookie.RefreshTokenCookieService;
import com.tyclick.chroniquesdetyrieback.auth.dto.request.LoginRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.request.RegisterRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.LoginResponse;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.RegisterResponse;
import com.tyclick.chroniquesdetyrieback.auth.model.LoginResult;
import com.tyclick.chroniquesdetyrieback.auth.service.AuthService;
import com.tyclick.chroniquesdetyrieback.common.exception.AuthenticationFailedException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult loginResult = authService.login(request);

        ResponseCookie refreshTokenCookie
                = refreshTokenCookieService.create(
                loginResult.rawRefreshToken(),
                request.isRememberMe()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString()
        );

        return loginResult.response();
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(
            @CookieValue(
                    name = "refresh_token",
                    required = false
            ) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthenticationFailedException(
                    "Refresh token is missing"
            );
        }

        return authService.refresh(refreshToken);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @CookieValue(
                    name = "refresh_token",
                    required = false
            ) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie deletedCookie
                = refreshTokenCookieService.delete();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                deletedCookie.toString()
        );
    }
}
