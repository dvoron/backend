package com.example.backend.controller;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RefreshRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.service.impl.AuthServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponseDto register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/refresh")
    public AuthResponseDto refresh(@RequestBody RefreshRequestDto request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public void logout(Authentication authentication) {
        UUID sessionId = (UUID) authentication.getCredentials();
        authService.logout(sessionId);
    }
}
