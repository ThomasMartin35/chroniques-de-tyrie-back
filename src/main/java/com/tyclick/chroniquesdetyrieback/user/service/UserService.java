package com.tyclick.chroniquesdetyrieback.user.service;

import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.user.dto.request.UpdateProfileRequest;
import com.tyclick.chroniquesdetyrieback.user.dto.response.UserProfileResponse;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.mapper.UserMapper;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
}
