package com.example.backend.controller;

import com.example.backend.model.dto.DeleteUserRequestDto;
import com.example.backend.model.dto.UserDto;
import com.example.backend.model.dto.UserUpdateResponseDto;
import com.example.backend.model.dto.UpdateUserRequestDto;
import com.example.backend.model.dto.AuthResponseDto;
import com.example.backend.model.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "User Management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Value("${jwt.refresh-token-expiry-days:30}")
    private int refreshTokenExpiryDays;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
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

    @Operation(summary = "Get user by ID", description = "Retrieves user details by user ID")
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id, Authentication authentication) {
        verifyOwnership(id, authentication);
        User user = userService.getUserById(id);
        return mapToDto(user);
    }

    @Operation(summary = "Update user", description = "Updates existing user details by user ID")
    @PutMapping("/{id}")
    public UserDto updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDto userDto,
            Authentication authentication,
            HttpServletResponse response) {

        verifyOwnership(id, authentication);
        User user = userService.updateUser(id, userDto);
        
        if (userDto.getNewPassword() != null && !userDto.getNewPassword().trim().isEmpty()) {
            AuthResponseDto tokens = authService.issueTokenPair(user);
            setRefreshTokenCookie(response, tokens.getRefreshToken());
            return new UserUpdateResponseDto(user.getId(), user.getUsername(), user.getEmail(), tokens.getAccessToken());
        }
        
        return mapToDto(user);
    }

    @Operation(summary = "Delete user", description = "Deletes a user by user ID")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, @Valid @RequestBody DeleteUserRequestDto request, Authentication authentication) {
        verifyOwnership(id, authentication);
        userService.deleteUser(id, request.getPassword());
    }

    private void verifyOwnership(Long id, Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();
        if (!id.equals(authenticatedUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
    }

    private UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }
}
