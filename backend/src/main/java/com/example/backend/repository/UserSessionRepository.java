package com.example.backend.repository;

import com.example.backend.model.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);
    
    java.util.List<UserSession> findByUserId(Long userId);
    
    void deleteByExpiresAtBefore(java.time.Instant expiry);
    
    void deleteByUserId(Long userId);
}
