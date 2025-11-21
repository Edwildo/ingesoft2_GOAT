package com.goat.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para la solicitud de verificación de OTP.
 */
public class VerifyOtpRequest {
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    private String email;

    @NotBlank(message = "El código OTP es requerido")
    private String otp;

    @NotBlank(message = "El propósito es requerido")
    @Pattern(regexp = "REGISTER|LOGIN|EMAIL_CONFIRMATION|RESET_PASSWORD", 
             message = "El propósito debe ser: REGISTER, LOGIN, EMAIL_CONFIRMATION o RESET_PASSWORD")
    private String purpose;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String email, String otp, String purpose) {
        this.email = email;
        this.otp = otp;
        this.purpose = purpose;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}

