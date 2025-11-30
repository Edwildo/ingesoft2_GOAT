package com.goat.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO inmutable para la solicitud de login.
 */
public record LoginRequest(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    String email,
    
    @NotBlank(message = "La contraseña es requerida")
    String password
) {}

