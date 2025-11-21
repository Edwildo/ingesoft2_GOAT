package com.goat.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para la solicitud de generación de OTP.
 */
public class GenerateOtpRequest {
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El propósito es requerido")
    @Pattern(regexp = "REGISTER|LOGIN|EMAIL_CONFIRMATION|RESET_PASSWORD", 
             message = "El propósito debe ser: REGISTER, LOGIN, EMAIL_CONFIRMATION o RESET_PASSWORD")
    private String purpose;

    public GenerateOtpRequest() {
    }

    public GenerateOtpRequest(String email, String purpose) {
        this.email = email;
        this.purpose = purpose;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}

