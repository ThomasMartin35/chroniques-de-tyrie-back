package com.tyclick.chroniquesdetyrieback.auth.service;

import com.tyclick.chroniquesdetyrieback.auth.dto.request.RegisterRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.RegisterResponse;
import com.tyclick.chroniquesdetyrieback.auth.mapper.AuthMapper;
import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.entity.UserRole;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
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
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Call the register method and assert that it throws a BusinessException with the expected message
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request)
        );

        // Verify that the exception message is as expected
        assertEquals("Email already in use", exception.getMessage());

        // Verify that the userRepository's save method was never called since the registration should fail
        verify(userRepository).existsByEmail(request.getEmail());
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
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        // Call the register method and assert that it throws a BusinessException with the expected message
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request)
        );

        // Verify that the exception message is as expected
        assertEquals("Username already in use", exception.getMessage());

        // Verify that the userRepository's save method was never called since the registration should fail
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByUsername(request.getUsername());
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
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);

        // Call the register method and assert that it throws a BusinessException with the expected message
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.register(request)
        );

        // Verify that the exception message is as expected
        assertEquals("Passwords do not match", exception.getMessage());

        // Verify that the userRepository's save method was never called since the registration should fail
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository).existsByUsername(request.getUsername());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

}
