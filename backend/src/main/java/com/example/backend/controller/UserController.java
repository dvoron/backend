package com.example.backend.controller;

import com.example.backend.model.dto.UserDto;
import com.example.backend.model.dto.UpdateUserRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "User Management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
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
            Authentication authentication) {

        verifyOwnership(id, authentication);
        User user = userService.updateUser(id, userDto);
        return mapToDto(user);
    }

    @Operation(summary = "Delete user", description = "Deletes a user by user ID")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, @Valid @RequestBody com.example.backend.model.dto.DeleteUserRequestDto request, Authentication authentication) {
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
