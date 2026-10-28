package com.example.backend.controller;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiry-days:30}")
    private int refreshTokenExpiryDays;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken != null) {
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/api/auth")
                    .maxAge(refreshTokenExpiryDays * 24L * 60L * 60L)
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Operation(summary = "Login user", description = "Authenticates user credentials and returns JWT access and refresh tokens")
    @SecurityRequirements()
    @PostMapping("/login")
    public AuthResponseDto login(@Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.login(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }

    @Operation(summary = "Register user", description = "Registers a new user and returns JWT access and refresh tokens")
    @SecurityRequirements()
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@Valid @RequestBody RegisterRequestDto request, HttpServletResponse response) {
        AuthResponseDto authResponse = authService.register(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }

    @Operation(summary = "Refresh JWT token", description = "Obtains new JWT tokens using a valid refresh token")
    @PostMapping("/refresh")
    public AuthResponseDto refresh(@CookieValue(name = "refreshToken", required = false) String token,
                                   HttpServletResponse response) {
        if (token == null || token.trim().isEmpty()) {
            throw new MissingRefreshTokenException("Refresh token is missing");
        }
        AuthResponseDto authResponse = authService.refresh(token);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }

    @Operation(summary = "Logout user", description = "Invalidates current user session")
    @PostMapping("/logout")
    public void logout(@Parameter(hidden = true) Authentication authentication, 
                       @CookieValue(name = "refreshToken", required = false) String refreshToken,
                       HttpServletResponse response) {
        if (authentication != null && authentication.getCredentials() instanceof UUID) {
            UUID sessionId = (UUID) authentication.getCredentials();
            authService.logout(sessionId);
        } else if (refreshToken != null && !refreshToken.trim().isEmpty()) {
            authService.logoutByRefreshToken(refreshToken);
        }
        clearRefreshTokenCookie(response);
    }
}
