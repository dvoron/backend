package com.example.backend.service;

import com.example.backend.model.dto.SessionCreateResult;

import java.util.UUID;

public interface SessionService {

    SessionCreateResult createSession(Long userId);

    SessionCreateResult rotateSession(String rawRefreshToken);

    void revokeSession(UUID sessionId);
}
