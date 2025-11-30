package com.goat.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO inmutable para la solicitud de verificación de OTP.
 */
public record VerifyOtpRequest(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    String email,
    
    @NotBlank(message = "El código OTP es requerido")
    String otp,
    
    @NotBlank(message = "El propósito es requerido")
    @Pattern(regexp = "REGISTER|LOGIN|EMAIL_CONFIRMATION|RESET_PASSWORD", 
             message = "El propósito debe ser: REGISTER, LOGIN, EMAIL_CONFIRMATION o RESET_PASSWORD")
    String purpose
) {}

