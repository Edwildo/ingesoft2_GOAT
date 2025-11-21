package com.goat.identity.application.dto;

import java.util.UUID;

/**
 * DTO para la respuesta de creación de usuario exitosa.
 */
public class CreateUserResponse {
    private UUID id;
    private String email;
    private Boolean emailConfirmed;
    private Boolean isActive;

    public CreateUserResponse() {
    }

    public CreateUserResponse(UUID id, String email, Boolean emailConfirmed, Boolean isActive) {
        this.id = id;
        this.email = email;
        this.emailConfirmed = emailConfirmed;
        this.isActive = isActive;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(Boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}

