package com.example.backend.controller;

import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.dto.RefreshRequestDto;
import com.example.backend.model.dto.RegisterRequestDto;
import com.example.backend.service.impl.AuthServiceImpl;
import com.example.backend.service.impl.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import org.springframework.context.annotation.Import;
import com.example.backend.config.SecurityConfig;
import com.example.backend.config.JwtAuthFilter;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authService;

    @MockitoBean
    private JwtService jwtService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loginReturnsOkAndTokens() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setLogin("test@example.com");
        request.setPassword("password");

        AuthResponseDto response = new AuthResponseDto("accessToken", "refreshToken");

        when(authService.login(any(LoginRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void registerReturnsOkAndTokens() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password");

        AuthResponseDto response = new AuthResponseDto("accessToken", "refreshToken");

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void refreshReturnsOkAndTokens() throws Exception {
        RefreshRequestDto request = new RefreshRequestDto();
        request.setRefreshToken("oldRefreshToken");

        AuthResponseDto response = new AuthResponseDto("newAccessToken", "newRefreshToken");

        when(authService.refresh("oldRefreshToken")).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("newRefreshToken"));
    }

    @Test
    void logoutReturnsOk() throws Exception {
        UUID sessionId = UUID.randomUUID();

        mockMvc.perform(post("/api/auth/logout")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, sessionId, List.of(new SimpleGrantedAuthority("ROLE_USER"))))))
                .andExpect(status().isOk());

        verify(authService).logout(sessionId);
    }
}
