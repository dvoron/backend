package com.example.backend.service.impl;

import com.example.backend.model.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-that-must-be-at-least-32-chars-long");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiryMinutes", 15);
    }

    @Test
    void generateTokenReturnsValidToken() {
        User user = new User();
        user.setId(1L);
        user.setName("testuser");
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateToken(user, sessionId);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void validateWithValidTokenReturnsClaims() {
        User user = new User();
        user.setId(1L);
        user.setName("testuser");
        UUID sessionId = UUID.randomUUID();

        String token = jwtService.generateToken(user, sessionId);
        Claims claims = jwtService.validate(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("username")).isEqualTo("testuser");
        assertThat(claims.get("sid")).isEqualTo(sessionId.toString());
        assertThat(claims.get("role")).isEqualTo("USER");
    }

    @Test
    void validateWithInvalidTokenThrowsException() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtService.validate(invalidToken))
                .isInstanceOf(Exception.class);
    }
}
