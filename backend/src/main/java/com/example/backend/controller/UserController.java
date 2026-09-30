package com.example.backend.controller;

import com.example.backend.model.dto.UserDto;
import com.example.backend.model.entity.User;
import com.example.backend.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "User Management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users")
    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getAllUsers().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get user by ID", description = "Retrieves user details by user ID")
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id, org.springframework.security.core.Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();
        if (!id.equals(authenticatedUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }
        User user = userService.getUserById(id);
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    @Operation(summary = "Update user", description = "Updates existing user details by user ID")
    @PutMapping("/{id}")
    public UserDto updateUser(
            @PathVariable Long id,
            @RequestBody com.example.backend.model.dto.UpdateUserRequestDto userDto,
            org.springframework.security.core.Authentication authentication) {

        Long authenticatedUserId = (Long) authentication.getPrincipal();
        if (!id.equals(authenticatedUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }

        User user = userService.updateUser(id, userDto);
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    @Operation(summary = "Delete user", description = "Deletes a user by user ID")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, org.springframework.security.core.Authentication authentication) {
        Long authenticatedUserId = (Long) authentication.getPrincipal();
        if (!id.equals(authenticatedUserId)) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied");
        }
        userService.deleteUser(id);
    }
}
