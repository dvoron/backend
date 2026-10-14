package com.example.backend.service.impl;

import com.example.backend.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry-minutes:15}")
    private int accessTokenExpiryMinutes;

    private SecretKey cachedKey;
    private final Environment environment;

    public JwtService(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if ("change-me-in-production-must-be-at-least-32-chars".equals(secret)) {
            boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
            if (isProd) {
                throw new IllegalStateException("JWT secret must be configured in production!");
            }
        }
        this.cachedKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user, UUID sessionId) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getName())
                .claim("sid", sessionId.toString())
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(accessTokenExpiryMinutes))))
                .signWith(cachedKey)
                .compact();
    }

    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(cachedKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
