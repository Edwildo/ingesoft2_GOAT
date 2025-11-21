package com.goat.identity.ports;

import com.goat.identity.domain.valueobjects.Email;

/**
 * Puerto (interfaz) para validar códigos OTP.
 * Define la operación de validación sin depender de implementación.
 */
public interface OtpValidationPort {
    /**
     * Valida un código OTP para un email y propósito específico.
     *
     * @param email Email del usuario
     * @param otp Código OTP a validar
     * @param purpose Propósito del OTP (REGISTER, LOGIN, EMAIL_CONFIRMATION, RESET_PASSWORD)
     * @return true si el OTP es válido, false en caso contrario
     */
    boolean validateOtp(Email email, String otp, String purpose);
}

