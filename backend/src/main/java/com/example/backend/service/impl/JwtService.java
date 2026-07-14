package com.example.backend.service.impl;

import com.example.backend.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry-minutes:15}")
    private int accessTokenExpiryMinutes;

    public String generateToken(User user, UUID sessionId) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("sid", sessionId.toString())
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(accessTokenExpiryMinutes))))
                .signWith(getKey())
                .compact();
    }

    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
