package com.tyclick.chroniquesdetyrieback.user.service;

import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.user.dto.request.UpdateProfileRequest;
import com.tyclick.chroniquesdetyrieback.user.dto.response.UserProfileResponse;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.mapper.UserMapper;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldUpdateCurrentUserProfileSuccessfully() {
        UUID userId = UUID.randomUUID();

        // Create a mock UpdateProfileRequest with new values
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .username("NewUsername")
                .biography("New biography")
                .build();

        // Create a mock User entity representing the current user
        User user = User.builder()
                .id(userId)
                .username("OldUsername")
                .email("thomas@test.fr")
                .biography("Old biography")
                .build();

        // Create a mock UserProfileResponse representing the expected response after update
        UserProfileResponse response = UserProfileResponse.builder()
                .id(userId)
                .username("ThomasUpdated")
                .email("thomas@test.fr")
                .biography("Nouvelle biographie")
                .build();

        // Mock the behavior of the userRepository and userMapper
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("NewUsername")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserProfileResponse(user)).thenReturn(response);

        // Call the method under test
        UserProfileResponse result = userService.updateCurrentUserProfile(updateProfileRequest, userId);

        // Verify the result
        verify(userRepository).findById(userId);
        verify(userRepository).existsByUsername("NewUsername");
        verify(userMapper).updateUserFromRequest(updateProfileRequest, user);
        verify(userRepository).save(user);
        verify(userMapper).toUserProfileResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        UUID userId = UUID.randomUUID();

        // Create a mock UpdateProfileRequest with a username that already exists
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .username("UsernameUsed")
                .build();

        // Create a mock User entity representing the current user
        User user = User.builder()
                .id(userId)
                .username("OldUsername")
                .email("thomas@test.fr")
                .build();

        // Mock the behavior of the userRepository to simulate that the username already exists
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("UsernameUsed")).thenReturn(true);

        // Call the method under test and assert that it throws a BusinessException
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateCurrentUserProfile(updateProfileRequest, userId)
        );

        verify(userRepository).findById(userId);
        verify(userRepository).existsByUsername("UsernameUsed");
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateUserFromRequest(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();

        // Create a mock UpdateProfileRequest
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .username("NewUsername")
                .build();

        // Mock the behavior of the userRepository to simulate that the user is not found
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Call the method under test and assert that it throws a BusinessException
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateCurrentUserProfile(updateProfileRequest, userId)
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(userId);
        verify(userRepository, never()).existsByUsername(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateUserFromRequest(any(), any());
    }

    @Test
    void shouldUpdateCurrentUserProfileWithoutChangingUsername() {
        UUID userId = UUID.randomUUID();

        // Create a mock UpdateProfileRequest without changing the username
        UpdateProfileRequest updateProfileRequest = UpdateProfileRequest.builder()
                .biography("Updated biography")
                .build();

        // Create a mock User entity representing the current user
        User user = User.builder()
                .id(userId)
                .username("OldUsername")
                .email("thomas@test.fr")
                .build();

        // Create a mock UserProfileResponse representing the expected response after update
        UserProfileResponse response = UserProfileResponse.builder()
                .id(userId)
                .username("OldUsername")
                .email("thomas@test.fr")
                .biography("Updated biography")
                .build();

        // Mock the behavior of the userRepository and userMapper
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserProfileResponse(user)).thenReturn(response);

        // Call the method under test
        UserProfileResponse result = userService.updateCurrentUserProfile(updateProfileRequest, userId);

        assertEquals("OldUsername", result.getUsername());
        assertEquals("Updated biography", result.getBiography());

        // Verify the result
        verify(userRepository).findById(userId);
        verify(userRepository, never()).existsByUsername(any());
        verify(userMapper).updateUserFromRequest(updateProfileRequest, user);
        verify(userRepository).save(user);
        verify(userMapper).toUserProfileResponse(user);

    }

    @Test
    void shouldNotCheckUsernameUniquenessWhenUsernameIsUnchanged() {
        UUID userId = UUID.randomUUID();

        // Create a mock UpdateProfileRequest with the same username as the current user
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .username("Thomas")
                .build();

        // Create a mock User entity representing the current user
        User user = User.builder()
                .id(userId)
                .username("Thomas")
                .build();

        // Create a mock UserProfileResponse representing the expected response after update
        UserProfileResponse response = UserProfileResponse.builder()
                .id(userId)
                .username("Thomas")
                .build();

        // Mock the behavior of the userRepository and userMapper
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserProfileResponse(user)).thenReturn(response);

        // Call the method under test
        userService.updateCurrentUserProfile(request, userId);

        // Verify that the username uniqueness check was not performed since the username is unchanged
        verify(userRepository, never()).existsByUsername(any());
        verify(userMapper).updateUserFromRequest(request, user);
        verify(userRepository).save(user);
    }
}