package com.example.backend.service.impl;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.model.dto.SessionCreateResult;
import com.example.backend.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl {

    private final UserServiceImpl userService;
    private final JwtService jwtService;
    private final SessionServiceImpl sessionService;

    public AuthServiceImpl(UserServiceImpl userService, JwtService jwtService, SessionServiceImpl sessionService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    public AuthResponseDto login(LoginRequestDto request) {
        User user = userService.signIn(request);
        return issueTokenPair(user);
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        User created = userService.createUser(request);
        return issueTokenPair(created);
    }

    public AuthResponseDto refresh(String rawRefreshToken) {
        SessionCreateResult session = sessionService.rotateSession(rawRefreshToken);
        User user = userService.getUserById(session.userId());
        String accessToken = jwtService.generateToken(user, session.sessionId());
        return new AuthResponseDto(accessToken, session.rawRefreshToken());
    }

    public void logout(UUID sessionId) {
        sessionService.revokeSession(sessionId);
    }

    private AuthResponseDto issueTokenPair(User user) {
        SessionCreateResult session = sessionService.createSession(user.getId());
        String accessToken = jwtService.generateToken(user, session.sessionId());
        return new AuthResponseDto(accessToken, session.rawRefreshToken());
    }
}
