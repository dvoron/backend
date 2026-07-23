package com.example.backend.controller;

import com.example.backend.model.dto.LoginRequestDto;
import com.example.backend.model.entity.User;
import com.example.backend.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Management", description = "Endpoints for managing users")
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserServiceImpl userService;

    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Operation(summary = "Hello check", description = "Simple health/hello endpoint for users")
    @GetMapping("/userHello")
    public String helloUser() {
        return "Hello from Spring Boot. If you see this front got a response from backend";
    }

    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users")
    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Get user by ID", description = "Retrieves user details by user ID")
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Sign in user", description = "Legacy sign-in endpoint returning user object")
    @PostMapping("/login")
    public User signIn(@RequestBody LoginRequestDto userLoginRequest) {
        return userService.signIn(userLoginRequest);
    }

    @Operation(summary = "Create user", description = "Creates a new user entity")
    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @Operation(summary = "Update user", description = "Updates existing user details by user ID")
    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User user) {

        return userService.updateUser(id, user);
    }

    @Operation(summary = "Delete user", description = "Deletes a user by user ID")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
