package com.example.backend.model.dto;

public class UserDto {
    private Long id;
    private String username;
    private String email;

    public UserDto() {

    }
    public UserDto(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }
}
