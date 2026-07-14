package com.example.backend.repository;

import com.example.backend.model.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHashAndRevokedFalse(String refreshTokenHash);
}
