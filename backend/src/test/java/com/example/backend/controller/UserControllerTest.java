package com.example.backend.controller;

import com.example.backend.config.JwtAuthFilter;
import com.example.backend.config.SecurityConfig;
import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.dto.DeleteUserRequestDto;
import com.example.backend.model.dto.UpdateUserRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.service.JwtService;
import com.example.backend.service.SessionService;
import com.example.backend.service.UserService;
import com.example.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private SessionService sessionService;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void getUserWithMatchingIdReturnsOkAndUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserWithNonMatchingIdReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/users/2")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserWithMatchingIdReturnsOkAndUpdatedUser() throws Exception {
        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setUsername("updateduser");
        request.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(eq(1L), any(UpdateUserRequestDto.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void updateUserWithPasswordChangeReturnsTokens() throws Exception {
        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setOldPassword("oldpassword");
        request.setNewPassword("newpassword");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");

        AuthResponseDto authResponse = new AuthResponseDto("newAccessToken", "newRefreshToken");

        when(userService.updateUser(eq(1L), any(UpdateUserRequestDto.class))).thenReturn(updatedUser);
        when(authService.issueTokenPair(any(User.class))).thenReturn(authResponse);

        mockMvc.perform(put("/api/users/1")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie().value("refreshToken", "newRefreshToken"));
    }

    @Test
    void updateUserWithNonMatchingIdReturnsForbidden() throws Exception {
        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setUsername("updateduser");

        mockMvc.perform(put("/api/users/2")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUserWithMatchingIdReturnsOk() throws Exception {
        DeleteUserRequestDto request = new DeleteUserRequestDto();
        request.setPassword("password");

        mockMvc.perform(delete("/api/users/1")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L, "password");
    }

    @Test
    void deleteUserWithNonMatchingIdReturnsForbidden() throws Exception {
        DeleteUserRequestDto request = new DeleteUserRequestDto();
        request.setPassword("password");

        mockMvc.perform(delete("/api/users/2")
                .with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
