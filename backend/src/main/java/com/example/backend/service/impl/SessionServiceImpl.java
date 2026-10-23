package com.example.backend.service.impl;

import com.example.backend.exception.InvalidRefreshTokenException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.dto.SessionCreateResult;
import com.example.backend.model.entity.UserSession;
import com.example.backend.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class SessionServiceImpl {

    private final UserSessionRepository sessionRepository;

    @Value("${jwt.refresh-token-expiry-days:30}")
    private int refreshTokenExpiryDays;

    public SessionServiceImpl(UserSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public SessionCreateResult createSession(Long userId) {
        String rawToken = UUID.randomUUID().toString();

        UserSession session = new UserSession();
        session.setUserId(userId);
        session.setRefreshTokenHash(hash(rawToken));
        session.setCreatedAt(Instant.now());
        session.setExpiresAt(Instant.now().plus(Duration.ofDays(refreshTokenExpiryDays)));
        session.setRevoked(false);

        UserSession saved = sessionRepository.save(session);
        return new SessionCreateResult(saved.getId(), rawToken, userId);
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public SessionCreateResult rotateSession(String rawRefreshToken) {
        UserSession session = sessionRepository
                .findByRefreshTokenHash(hash(rawRefreshToken))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (session.isRevoked()) {
            revokeAllUserSessions(session.getUserId());
            throw new InvalidRefreshTokenException();
        }

        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setRevoked(true);
            sessionRepository.save(session);
            throw new InvalidRefreshTokenException();
        }

        session.setRevoked(true);
        sessionRepository.save(session);

        return createSession(session.getUserId());
    }

    public void revokeSession(UUID sessionId) {
        if (sessionId == null) {
            return;
        }
        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        session.setRevoked(true);
        sessionRepository.save(session);
    }

    public void revokeAllUserSessions(Long userId) {
        java.util.List<UserSession> sessions = sessionRepository.findByUserId(userId);
        for (UserSession session : sessions) {
            session.setRevoked(true);
        }
        sessionRepository.saveAll(sessions);
    }

    public boolean isSessionValid(UUID sessionId, Long userId) {
        if (sessionId == null || userId == null) return false;
        return sessionRepository.findById(sessionId)
                .map(session -> !session.isRevoked() && session.getExpiresAt().isAfter(Instant.now()) && session.getUserId().equals(userId))
                .orElse(false);
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
