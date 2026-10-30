package com.example.backend.model.dto;

public class UserUpdateResponseDto extends UserDto {
    private String accessToken;

    public UserUpdateResponseDto(Long id, String username, String email, String accessToken) {
        super(id, username, email);
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}