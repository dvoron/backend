package com.example.backend.controller;


import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public User login(@RequestBody LoginRequestDto userLoginRequest) {
        return authService.login();
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return authService.register();
    }
}
