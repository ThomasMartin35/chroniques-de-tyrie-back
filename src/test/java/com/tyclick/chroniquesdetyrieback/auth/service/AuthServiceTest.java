package com.tyclick.chroniquesdetyrieback.auth.service;

import com.tyclick.chroniquesdetyrieback.auth.dto.request.LoginRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.request.RegisterRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.LoginResponse;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.RegisterResponse;
import com.tyclick.chroniquesdetyrieback.auth.jwt.JwtService;
import com.tyclick.chroniquesdetyrieback.auth.mapper.AuthMapper;
import com.tyclick.chroniquesdetyrieback.auth.model.LoginResult;
import com.tyclick.chroniquesdetyrieback.auth.refreshtoken.entity.RefreshToken;
import com.tyclick.chroniquesdetyrieback.auth.refreshtoken.model.CreatedRefreshToken;
import com.tyclick.chroniquesdetyrieback.auth.refreshtoken.service.RefreshTokenService;
import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import com.tyclick.chroniquesdetyrieback.common.exception.AuthenticationFailedException;
import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.entity.UserRole;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = RegisterRequest.builder()
                .username("Thomas")
                .email("thomas@test.fr")
                .password("password")
                .confirmPassword("password")
                .build();

        // Mock the behavior of the userRepository and passwordEncoder to simulate successful registration
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");

        // Mock the behavior of the authMapper to return a User object when mapping from RegisterRequest
        User user = User.builder().build();
        when(authMapper.toUser(request)).thenReturn(user);

        // Call the register method and assert that it returns a successful response
        RegisterResponse response = authService.register(request);

        // Verify that the response message is as expected and that the userRepository's save method was called with the correct user
        assertEquals("User registered successfully", response.getMessage());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals(UserRole.ROLE_MEMBER, user.getRole());
        assertEquals(true, user.getIsActive());

        // Verify that the userRepository's save method was called with the correct user
        verify(passwordEncoder).encode(request.getPassword());
        verify(authMapper).toUser(request);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("Thomas")
                .email("thomas@test.fr")
                .password("password")
                .confirmPassword("password")
                .build();

        // Mock the behavior of the userRepository to simulate that the email already exists
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        // Call the register method and assert that it throws a BusinessException with the expected message
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request)
        );

        // Verify that the exception message is as expected
        assertEquals("Email already in use", exception.getMessage());

        // Verify that the userRepository's save method was never called since the registration should fail
        verify(userRepository).existsByEmailIgnoreCase(request.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        RegisterRequest request = RegisterRequest.builder()
                .username("Thomas")
                .email("thomas@test.fr")
                .password("password")
                .confirmPassword("password")
                .build();

        // Mock the behavior of the userRepository to simulate that the username already exists
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(true);

        // Call the register method and assert that it throws a BusinessException with the expected message
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request)
        );

        // Verify that the exception message is as expected
        assertEquals("Username already in use", exception.getMessage());

        // Verify that the userRepository's save method was never called since the registration should fail
        verify(userRepository).existsByEmailIgnoreCase(request.getEmail());
        verify(userRepository).existsByUsernameIgnoreCase(request.getUsername());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
        RegisterRequest request = RegisterRequest.builder()
                .username("Thomas")
                .email("thomas@test.fr")
                .password("password")
                .confirmPassword("differentPassword")
                .build();

        // Mock the behavior of the userRepository to simulate that the email and username do not exist
        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(false);

        // Call the register method and assert that it throws a BusinessException with the expected message
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request)
        );

        // Verify that the exception message is as expected
        assertEquals("Passwords do not match", exception.getMessage());

        // Verify that the userRepository's save method was never called since the registration should fail
        verify(userRepository).existsByEmailIgnoreCase(request.getEmail());
        verify(userRepository).existsByUsernameIgnoreCase(request.getUsername());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = LoginRequest.builder()
                .email("thomas@test.fr")
                .password("password123!")
                .rememberMe(true)
                .build();

        User user = User.builder()
                .email("thomas@test.fr")
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        RefreshToken refreshToken = new RefreshToken();

        CreatedRefreshToken createdRefreshToken =
                new CreatedRefreshToken(
                        "mocked-refresh-token",
                        refreshToken
                );

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(customUserDetails);

        when(jwtService.generateToken(customUserDetails))
                .thenReturn("mocked-jwt-token");

        when(refreshTokenService.create(user, true))
                .thenReturn(createdRefreshToken);

        LoginResult result = authService.login(request);

        assertEquals(
                "mocked-jwt-token",
                result.response().getToken()
        );

        assertEquals(
                "Bearer",
                result.response().getTokenType()
        );

        assertEquals(
                "mocked-refresh-token",
                result.rawRefreshToken()
        );

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(jwtService).generateToken(customUserDetails);
        verify(refreshTokenService).create(user, true);
    }

    @Test
    void shouldThrowExceptionWhenLoginFails() {
        LoginRequest request = LoginRequest.builder()
                .email("thomas@test.fr")
                .password("wrongpassword")
                .rememberMe(true)
                .build();

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(
                new BadCredentialsException("Authentication failed")
        );

        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(jwtService, never()).generateToken(any());
        verify(refreshTokenService, never()).create(any(), anyBoolean());
    }

    @Test
    void shouldRefreshAccessTokenSuccessfully() {
        User user = User.builder()
                .email("thomas@test.fr")
                .build();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(
                Instant.now().plusSeconds(3600)
        );

        when(refreshTokenService.findByRawToken("raw-refresh-token"))
                .thenReturn(Optional.of(refreshToken));

        when(refreshTokenService.isValid(refreshToken))
                .thenReturn(true);

        when(jwtService.generateToken(any(CustomUserDetails.class)))
                .thenReturn("new-access-token");

        LoginResponse response =
                authService.refresh("raw-refresh-token");

        assertEquals("new-access-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());

        verify(refreshTokenService)
                .findByRawToken("raw-refresh-token");

        verify(refreshTokenService)
                .isValid(refreshToken);

        verify(jwtService)
                .generateToken(any(CustomUserDetails.class));
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsNotFound() {
        when(refreshTokenService.findByRawToken("unknown-token"))
                .thenReturn(Optional.empty());

        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authService.refresh("unknown-token")
        );

        assertEquals(
                "Invalid refresh token",
                exception.getMessage()
        );

        verify(refreshTokenService)
                .findByRawToken("unknown-token");

        verify(refreshTokenService, never())
                .isValid(any());

        verify(jwtService, never())
                .generateToken(any());
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
        RefreshToken refreshToken = new RefreshToken();

        when(refreshTokenService.findByRawToken("invalid-token"))
                .thenReturn(Optional.of(refreshToken));

        when(refreshTokenService.isValid(refreshToken))
                .thenReturn(false);

        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> authService.refresh("invalid-token")
        );

        assertEquals(
                "Invalid or expired refresh token",
                exception.getMessage()
        );

        verify(refreshTokenService)
                .isValid(refreshToken);

        verify(jwtService, never())
                .generateToken(any());
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() {

        RefreshToken refreshToken = new RefreshToken();

        when(refreshTokenService.findByRawToken("refresh-token"))
                .thenReturn(Optional.of(refreshToken));

        authService.logout("refresh-token");

        verify(refreshTokenService)
                .findByRawToken("refresh-token");

        verify(refreshTokenService)
                .revoke(refreshToken);
    }

    @Test
    void shouldDoNothingWhenRefreshTokenDoesNotExist() {

        when(refreshTokenService.findByRawToken("unknown"))
                .thenReturn(Optional.empty());

        authService.logout("unknown");

        verify(refreshTokenService)
                .findByRawToken("unknown");

        verify(refreshTokenService, never())
                .revoke(any());
    }
}
