package com.goat.identity.application.dto;

import java.util.UUID;

/**
 * DTO inmutable para la respuesta de creación de usuario exitosa.
 */
public record CreateUserResponse(
    UUID id,
    String email,
    Boolean emailConfirmed,
    Boolean isActive
) {}

