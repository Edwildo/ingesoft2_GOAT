package com.goat.identity.adapters.security;

import java.util.List;
import java.util.UUID;

/**
 * Detalles adicionales almacenados en el objeto Authentication.
 * Permite acceder fácilmente al userId, email y roles desde el SecurityContext.
 */
public class JwtAuthenticationDetails {
    private final UUID userId;
    private final String email;
    private final List<String> roles;

    public JwtAuthenticationDetails(UUID userId, String email, List<String> roles) {
        this.userId = userId;
        this.email = email;
        this.roles = roles != null ? List.copyOf(roles) : List.of();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
