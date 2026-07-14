package com.example.backend.service;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.entity.User;

import java.util.UUID;

public interface AuthService {

    AuthResponseDto login(LoginRequestDto request);

    AuthResponseDto register(User user);

    AuthResponseDto refresh(String rawRefreshToken);

    void logout(UUID sessionId);
}
