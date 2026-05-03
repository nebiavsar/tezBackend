package com.example.demo.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserClientResponse;
import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.security.JwtService;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public UserClientResponse signup(SignupRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username is already in use.");
        }
        if (userService.existsByEmail(request.getEmail().trim())) {
            throw new ConflictException("Email is already in use.");
        }

        User user = userService.save(User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .fullName(request.getFullName().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.TEACHER)
                .build());

        return buildUserClientResponse(user, jwtService.generateToken(user));
    }

    @Override
    public UserClientResponse login(LoginRequest request) {
        String usernameOrEmail = request.getUsernameOrEmail() == null ? "" : request.getUsernameOrEmail().trim();
        User user = userService.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Username/email or password is incorrect."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Username/email or password is incorrect.");
        }

        return buildUserClientResponse(user, jwtService.generateToken(user));
    }

    @Override
    public UserClientResponse getCurrentUser(Long userId, String accessToken) {
        return buildUserClientResponse(userService.getById(userId), accessToken);
    }

    private UserClientResponse buildUserClientResponse(User user, String accessToken) {
        return UserClientResponse.builder()
                .fullName(user.getFullName())
                .accessToken(accessToken)
                .build();
    }
}
