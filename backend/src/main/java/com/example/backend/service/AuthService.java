package com.example.backend.service;

import com.example.backend.exception.InvalidRefreshTokenException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.model.dto.SessionCreateResult;
import com.example.backend.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final SessionService sessionService;

    public AuthService(UserService userService, JwtService jwtService, SessionService sessionService) {
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
        User user;
        try {
            user = userService.getUserById(session.userId());
        } catch (ResourceNotFoundException e) {
            throw new InvalidRefreshTokenException();
        }
        String accessToken = jwtService.generateToken(user, session.sessionId());
        return new AuthResponseDto(accessToken, session.rawRefreshToken());
    }

    public void logout(UUID sessionId) {
        sessionService.revokeSession(sessionId);
    }

    public void logoutByRefreshToken(String rawRefreshToken) {
        sessionService.revokeSessionByToken(rawRefreshToken);
    }

    public AuthResponseDto issueTokenPair(User user) {
        SessionCreateResult session = sessionService.createSession(user.getId());
        String accessToken = jwtService.generateToken(user, session.sessionId());
        return new AuthResponseDto(accessToken, session.rawRefreshToken());
    }
}
