package com.example.backend.service.impl;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.model.dto.SessionCreateResult;
import com.example.backend.model.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserServiceImpl userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private SessionServiceImpl sessionService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginReturnsAuthResponseDto() {
        LoginRequestDto request = new LoginRequestDto();
        request.setLogin("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        UUID sessionId = UUID.randomUUID();
        SessionCreateResult sessionResult = new SessionCreateResult(sessionId, "rawRefreshToken", 1L);

        when(userService.signIn(request)).thenReturn(user);
        when(sessionService.createSession(1L)).thenReturn(sessionResult);
        when(jwtService.generateToken(user, sessionId)).thenReturn("accessToken");

        AuthResponseDto response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("rawRefreshToken");
    }

    @Test
    void registerReturnsAuthResponseDto() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        UUID sessionId = UUID.randomUUID();
        SessionCreateResult sessionResult = new SessionCreateResult(sessionId, "rawRefreshToken", 1L);

        when(userService.createUser(request)).thenReturn(user);
        when(sessionService.createSession(1L)).thenReturn(sessionResult);
        when(jwtService.generateToken(user, sessionId)).thenReturn("accessToken");

        AuthResponseDto response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("rawRefreshToken");
    }

    @Test
    void refreshReturnsNewAuthResponseDto() {
        String rawRefreshToken = "oldRefreshToken";
        UUID sessionId = UUID.randomUUID();
        SessionCreateResult sessionResult = new SessionCreateResult(sessionId, "newRawRefreshToken", 1L);

        User user = new User();
        user.setId(1L);

        when(sessionService.rotateSession(rawRefreshToken)).thenReturn(sessionResult);
        when(userService.getUserById(1L)).thenReturn(user);
        when(jwtService.generateToken(user, sessionId)).thenReturn("newAccessToken");

        AuthResponseDto response = authService.refresh(rawRefreshToken);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRawRefreshToken");
    }

    @Test
    void logoutRevokesSession() {
        UUID sessionId = UUID.randomUUID();

        authService.logout(sessionId);

        verify(sessionService, times(1)).revokeSession(sessionId);
    }
}
