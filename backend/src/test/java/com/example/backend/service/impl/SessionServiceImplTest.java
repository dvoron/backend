package com.example.backend.service.impl;

import com.example.backend.exception.InvalidRefreshTokenException;
import com.example.backend.model.dto.SessionCreateResult;
import com.example.backend.model.entity.UserSession;
import com.example.backend.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private UserSessionRepository sessionRepository;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sessionService, "refreshTokenExpiryDays", 30);
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

    @Test
    void testCreateSession() {
        Long userId = 1L;
        UUID savedId = UUID.randomUUID();

        when(sessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            ReflectionTestUtils.setField(session, "id", savedId);
            return session;
        });

        SessionCreateResult result = sessionService.createSession(userId);

        assertNotNull(result);
        assertEquals(savedId, result.sessionId());
        assertEquals(userId, result.userId());
        assertNotNull(result.rawRefreshToken());

        ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
        verify(sessionRepository).save(captor.capture());
        
        UserSession savedSession = captor.getValue();
        assertEquals(userId, savedSession.getUserId());
        assertEquals(hash(result.rawRefreshToken()), savedSession.getRefreshTokenHash());
        assertFalse(savedSession.isRevoked());
        assertTrue(savedSession.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void testRotateSession_Success() {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);
        Long userId = 1L;
        UUID oldSessionId = UUID.randomUUID();

        UserSession oldSession = new UserSession();
        ReflectionTestUtils.setField(oldSession, "id", oldSessionId);
        oldSession.setUserId(userId);
        oldSession.setRefreshTokenHash(tokenHash);
        oldSession.setExpiresAt(Instant.now().plus(Duration.ofDays(10)));
        oldSession.setRevoked(false);

        when(sessionRepository.findByRefreshTokenHashAndRevokedFalse(tokenHash))
                .thenReturn(Optional.of(oldSession));
        
        when(sessionRepository.save(any(UserSession.class))).thenAnswer(invocation -> {
            UserSession session = invocation.getArgument(0);
            if (session.getId() == null) {
                ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
            }
            return session;
        });

        SessionCreateResult result = sessionService.rotateSession(rawToken);

        assertNotNull(result);
        assertEquals(userId, result.userId());
        assertNotNull(result.rawRefreshToken());

        // Verify old session was revoked
        assertTrue(oldSession.isRevoked());
        
        // Save is called twice: once to update old session, once to save new session
        verify(sessionRepository, times(2)).save(any(UserSession.class));
    }

    @Test
    void testRotateSession_TokenExpired() {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);
        Long userId = 1L;

        UserSession oldSession = new UserSession();
        ReflectionTestUtils.setField(oldSession, "id", UUID.randomUUID());
        oldSession.setUserId(userId);
        oldSession.setRefreshTokenHash(tokenHash);
        oldSession.setExpiresAt(Instant.now().minus(Duration.ofDays(1))); // Expired
        oldSession.setRevoked(false);

        when(sessionRepository.findByRefreshTokenHashAndRevokedFalse(tokenHash))
                .thenReturn(Optional.of(oldSession));

        assertThrows(InvalidRefreshTokenException.class, () -> sessionService.rotateSession(rawToken));

        // Verify old session was revoked
        assertTrue(oldSession.isRevoked());
        verify(sessionRepository).save(oldSession);
    }

    @Test
    void testRotateSession_TokenNotFoundOrRevoked() {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);

        when(sessionRepository.findByRefreshTokenHashAndRevokedFalse(tokenHash))
                .thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () -> sessionService.rotateSession(rawToken));

        verify(sessionRepository, never()).save(any(UserSession.class));
    }

    @Test
    void testRevokeSession_Success() {
        UUID sessionId = UUID.randomUUID();
        UserSession session = new UserSession();
        ReflectionTestUtils.setField(session, "id", sessionId);
        session.setRevoked(false);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        sessionService.revokeSession(sessionId);

        assertTrue(session.isRevoked());
        verify(sessionRepository).save(session);
    }

    @Test
    void testRevokeSession_NotFound() {
        UUID sessionId = UUID.randomUUID();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> sessionService.revokeSession(sessionId));
        assertEquals("Session not found", exception.getMessage());

        verify(sessionRepository, never()).save(any(UserSession.class));
    }
    
    @Test
    void testInvalidRefreshTokenException() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException();
        assertEquals("Refresh token is invalid or has expired", exception.getMessage());
    }
}