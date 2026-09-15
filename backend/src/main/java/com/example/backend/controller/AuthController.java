package com.example.backend.controller;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RefreshRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Authentication", description = "Endpoints for user login, registration, token refresh, and logout")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login user", description = "Authenticates user credentials and returns JWT access and refresh tokens")
    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @Operation(summary = "Register user", description = "Registers a new user and returns JWT access and refresh tokens")
    @PostMapping("/register")
    public AuthResponseDto register(@RequestBody RegisterRequestDto request) {
        return authService.register(request);
    }

    @Operation(summary = "Refresh JWT token", description = "Obtains new JWT tokens using a valid refresh token")
    @PostMapping("/refresh")
    public AuthResponseDto refresh(@RequestBody RefreshRequestDto request) {
        return authService.refresh(request.getRefreshToken());
    }

    @Operation(summary = "Logout user", description = "Invalidates current user session")
    @PostMapping("/logout")
    public void logout(@Parameter(hidden = true) Authentication authentication) {
        UUID sessionId = (UUID) authentication.getCredentials();
        authService.logout(sessionId);
    }
}
