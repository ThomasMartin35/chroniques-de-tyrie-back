package com.tyclick.chroniquesdetyrieback.user.service;

import com.tyclick.chroniquesdetyrieback.common.dto.response.MessageResponse;
import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.user.dto.request.ChangePasswordRequest;
import com.tyclick.chroniquesdetyrieback.user.dto.request.UpdateProfileRequest;
import com.tyclick.chroniquesdetyrieback.user.dto.response.UserProfileResponse;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.mapper.UserMapper;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Update the current user's profile information.
     * @param updateProfileRequest The request object containing the updated profile information.
     * @param userId The ID of the current user.
     * @return UserProfileResponse containing the updated user profile information.
     * @throws BusinessException if the user is not found or if the username is already taken
     */
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest updateProfileRequest, UUID userId) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (updateProfileRequest.getUsername() != null
                && !updateProfileRequest.getUsername().equals(currentUser.getUsername())) {

            if (userRepository.existsByUsername(updateProfileRequest.getUsername())) {
                throw new BusinessException("Username is already taken");
            }
        }

        userMapper.updateUserFromRequest(updateProfileRequest, currentUser);

        User updatedUser = userRepository.save(currentUser);

        return userMapper.toUserProfileResponse(updatedUser);
    }

    /**
     * Change the password of the current user.
     * @param userId The ID of the current user.
     * @param changePasswordRequest The request object containing the current password, new password, and confirmation of the new password.
     * @return MessageResponse indicating the success of the password change operation.
     * @throws BusinessException if the user is not found, if the current password is incorrect, or if the new password and confirmation do not match.
     */
    public MessageResponse changePassword(UUID userId, ChangePasswordRequest changePasswordRequest) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), currentUser.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), currentUser.getPasswordHash())) {
            throw new BusinessException("New password must be different from the current password");
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            throw new BusinessException("New password and confirm password do not match");
        }

        currentUser.setPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        currentUser.setLastPasswordChangeAt(Instant.now());

        userRepository.save(currentUser);

        return MessageResponse.builder()
                .message("Password changed successfully")
                .build();
    }

}
