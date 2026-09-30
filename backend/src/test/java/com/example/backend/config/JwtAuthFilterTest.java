package com.example.backend.config;

import com.example.backend.service.impl.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalWithValidTokenSetsAuthentication() throws ServletException, IOException {
        String token = "valid.token.here";
        UUID sessionId = UUID.randomUUID();
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        Claims claims = new DefaultClaims(Map.of(
                "sub", "1",
                "sid", sessionId.toString(),
                "role", "USER"
        ));
        
        when(jwtService.validate(token)).thenReturn(claims);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(1L);
        assertThat(auth.getCredentials()).isEqualTo(sessionId);
        
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternalWithNoAuthHeaderDoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternalWithInvalidTokenDoesNotSetAuthentication() throws ServletException, IOException {
        String token = "invalid.token.here";
        
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validate(token)).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
