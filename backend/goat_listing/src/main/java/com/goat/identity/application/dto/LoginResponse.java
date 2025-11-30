package com.goat.identity.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO inmutable para la respuesta de login exitoso.
 */
public record LoginResponse(
    String token,
    String email,
    List<String> roles,
    UUID userId
) {
    /**
     * Constructor compacto para asegurar inmutabilidad de la lista.
     */
    public LoginResponse {
        // Hacemos la lista inmutable para garantizar inmutabilidad completa
        roles = roles != null ? List.copyOf(roles) : List.of();
    }
}

