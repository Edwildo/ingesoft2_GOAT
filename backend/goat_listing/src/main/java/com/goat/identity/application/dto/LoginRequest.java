package com.goat.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO inmutable para la solicitud de login.
 */
public record LoginRequest(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    String email,
    
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    String password
) {}
