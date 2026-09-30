package com.example.backend.model.dto;

public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
