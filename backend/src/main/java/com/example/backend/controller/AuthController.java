package com.example.backend.controller;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RefreshRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.service.impl.AuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.backend.exception.MissingRefreshTokenException;

import java.util.UUID;

@Tag(name = "Authentication", description = "Endpoints for user login, registration, token refresh, and logout")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken != null) {
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Operation(summary = "Login user", description = "Authenticates user credentials and returns JWT access and refresh tokens")
    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }

    @Operation(summary = "Register user", description = "Registers a new user and returns JWT access and refresh tokens")
    @PostMapping("/register")
    public AuthResponseDto register(@RequestBody RegisterRequestDto request, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.register(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }

    @Operation(summary = "Refresh JWT token", description = "Obtains new JWT tokens using a valid refresh token")
    @PostMapping("/refresh")
    public AuthResponseDto refresh(@CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
                                   @RequestBody(required = false) RefreshRequestDto request,
                                   HttpServletResponse response) {
        String token = refreshTokenCookie != null ? refreshTokenCookie : (request != null ? request.getRefreshToken() : null);
        if (token == null || token.trim().isEmpty()) {
            throw new MissingRefreshTokenException("Refresh token is missing");
        }
        AuthResponseDto authResponse = authService.refresh(token);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }

    @Operation(summary = "Logout user", description = "Invalidates current user session")
    @PostMapping("/logout")
    public void logout(@Parameter(hidden = true) Authentication authentication, HttpServletResponse response) {
        UUID sessionId = (UUID) authentication.getCredentials();
        authService.logout(sessionId);
        clearRefreshTokenCookie(response);
    }
}
