package com.tyclick.chroniquesdetyrieback.auth.service;

import com.tyclick.chroniquesdetyrieback.auth.dto.request.LoginRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.request.RegisterRequest;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.LoginResponse;
import com.tyclick.chroniquesdetyrieback.auth.dto.response.RegisterResponse;
import com.tyclick.chroniquesdetyrieback.auth.jwt.JwtService;
import com.tyclick.chroniquesdetyrieback.auth.mapper.AuthMapper;
import com.tyclick.chroniquesdetyrieback.auth.security.CustomUserDetails;
import com.tyclick.chroniquesdetyrieback.common.exception.AuthenticationFailedException;
import com.tyclick.chroniquesdetyrieback.common.exception.BusinessException;
import com.tyclick.chroniquesdetyrieback.user.entity.User;
import com.tyclick.chroniquesdetyrieback.user.entity.UserRole;
import com.tyclick.chroniquesdetyrieback.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Registers a new user based on the provided RegisterRequest. It checks for existing email and username, validates password confirmation, and saves the new user to the repository.
     * @param request The RegisterRequest containing user registration details.
     * @return A RegisterResponse indicating the success of the registration process.
     * @throws BusinessException if the email or username is already in use, or if the password and confirm password do not match.
     */
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

    /**
     * Authenticates a user based on the provided LoginRequest. It generates a JWT token upon successful authentication.
     * @param request The LoginRequest containing user login details (email and password).
     * @return A LoginResponse containing the generated JWT token and its type.
     */
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
                throw new BusinessException("Authentication failed");
            }

            String token = jwtService.generateToken(userDetails);

            return LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .build();

        } catch (AuthenticationException exception) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }

}
