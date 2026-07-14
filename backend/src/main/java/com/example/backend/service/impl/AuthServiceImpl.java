package com.example.backend.service.impl;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.SessionCreateResult;
import com.example.backend.model.entity.User;
import com.example.backend.service.AuthService;
import com.example.backend.service.SessionService;
import com.example.backend.service.UserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public AuthServiceImpl(UserService userService, JwtService jwtService, SessionService sessionService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
    }

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userService.signIn(request);
        return issueTokenPair(user);
    }

    @Override
    public AuthResponseDto register(User user) {
        User created = userService.createUser(user);
        return issueTokenPair(created);
    }

    @Override
    public AuthResponseDto refresh(String rawRefreshToken) {
        SessionCreateResult session = sessionService.rotateSession(rawRefreshToken);
        User user = userService.getUserById(session.userId());
        String accessToken = jwtService.generateToken(user, session.sessionId());
        return new AuthResponseDto(accessToken, session.rawRefreshToken());
    }

    @Override
    public void logout(UUID sessionId) {
        sessionService.revokeSession(sessionId);
    }

    private AuthResponseDto issueTokenPair(User user) {
        SessionCreateResult session = sessionService.createSession(user.getId());
        String accessToken = jwtService.generateToken(user, session.sessionId());
        return new AuthResponseDto(accessToken, session.rawRefreshToken());
    }
}
