package com.goat.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de login.
 */
public class LoginRequest {
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

