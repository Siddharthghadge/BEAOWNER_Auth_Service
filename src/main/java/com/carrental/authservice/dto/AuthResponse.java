package com.carrental.authservice.dto;

public class AuthResponse {
    private String accessToken;
    private String role;
    private String refreshToken;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String role, String refreshToken) {
        this.accessToken = accessToken;
        this.role = role;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
