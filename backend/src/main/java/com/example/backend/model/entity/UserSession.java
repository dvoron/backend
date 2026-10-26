package com.example.backend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(indexes = {
    @Index(name = "idx_user_session_refresh_token_hash", columnList = "refreshTokenHash"),
    @Index(name = "idx_user_session_user_id", columnList = "userId")
})
public class UserSession {

    @Id
    @GeneratedValue
    private UUID id;

    private Long userId;

    private String refreshTokenHash;

    private Instant createdAt;

    private Instant expiresAt;

    private boolean revoked;

    private Instant revokedAt;

    @Version
    private Long version;

    public UUID getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRefreshTokenHash() {
        return refreshTokenHash;
    }

    public void setRefreshTokenHash(String refreshTokenHash) {
        this.refreshTokenHash = refreshTokenHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
