package com.goat.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para la solicitud de creación de usuario.
 * Permite asignar roles opcionales al usuario durante el registro.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    /**
     * Lista opcional de códigos de roles a asignar al usuario.
     * Ejemplos: ["SELLER"], ["BUYER"], ["CLIENT"], ["SELLER", "BUYER"]
     * Si no se proporciona, el usuario se crea sin roles.
     */
    private List<String> roles;

    public CreateUserRequest(String email, String password) {
        this.email = email;
        this.password = password;
        this.roles = new ArrayList<>();
    }

    /**
     * Retorna la lista de roles, inicializando una lista vacía si es null.
     */
    public List<String> getRoles() {
        return roles != null ? roles : new ArrayList<>();
    }
}

