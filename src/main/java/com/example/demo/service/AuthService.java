package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.UserClientResponse;

public interface AuthService {

    UserClientResponse signup(SignupRequest request);

    UserClientResponse login(LoginRequest request);

    UserClientResponse getCurrentUser(Long userId, String accessToken);
}
