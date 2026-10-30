package com.example.backend.model.dto;

import java.util.UUID;

public record SessionCreateResult(UUID sessionId, String rawRefreshToken, Long userId) {}
