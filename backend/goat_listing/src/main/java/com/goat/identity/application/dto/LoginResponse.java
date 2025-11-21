package com.goat.identity.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO para la respuesta de login exitoso.
 */
public class LoginResponse {
    private String token;
    private String email;
    private List<String> roles;
    private UUID userId;

    public LoginResponse() {
    }

    public LoginResponse(String token, String email, List<String> roles, UUID userId) {
        this.token = token;
        this.email = email;
        this.roles = roles;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}

