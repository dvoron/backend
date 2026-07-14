package com.example.backend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

@Entity
public class UserSession {

    @Id
    @GeneratedValue
    private UUID id;

    private Long userId;

    private String refreshTokenHash;

    private Instant expiresAt;

    private boolean revoked;
}
