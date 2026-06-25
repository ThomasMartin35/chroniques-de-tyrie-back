package com.tyclick.chroniquesdetyrieback.auth.service;

import com.tyclick.chroniquesdetyrieback.auth.dto.request.RegisterRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.RegisterResponse;
import com.tyclick.chroniquesdetyrieback.auth.mapper.AuthMapper;
import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.entity.UserRole;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Attempt to register with an already used email: {}", request.getEmail());
            throw new BusinessException("Email already in use");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Attempt to register with an already used username: {}", request.getUsername());
            throw new BusinessException("Username already in use");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Password and confirm password do not match for username: {}", request.getUsername());
            throw new BusinessException("Passwords do not match");
        }

        User user = authMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.ROLE_MEMBER);
        user.setIsActive(true);

        userRepository.save(user);
        return RegisterResponse.builder()
                .message("User registered successfully")
                .build();
    }
}
