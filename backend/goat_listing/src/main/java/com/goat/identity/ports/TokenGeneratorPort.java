package com.goat.identity.ports;

import java.util.List;
import java.util.UUID;

/**
 * Puerto (interfaz) para la generación de tokens JWT.
 * Define la operación de generación sin depender de implementación.
 */
public interface TokenGeneratorPort {
    /**
     * Genera un token JWT para un usuario autenticado.
     *
     * @param userId ID del usuario
     * @param email Email del usuario
     * @param roles Lista de roles del usuario
     * @return Token JWT
     */
    String generateToken(UUID userId, String email, List<String> roles);
}

