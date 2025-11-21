package com.goat.identity.ports;

import com.goat.identity.domain.valueobjects.Email;

/**
 * Puerto (interfaz) para generar códigos OTP.
 * Define la operación de generación sin depender de implementación.
 */
public interface OtpGenerationPort {
    /**
     * Genera un código OTP para un email y propósito específico.
     *
     * @param email Email del usuario
     * @param purpose Propósito del OTP (REGISTER, LOGIN, EMAIL_CONFIRMATION, RESET_PASSWORD)
     * @return true si el OTP fue generado exitosamente, false en caso contrario
     */
    boolean generateOtp(Email email, String purpose);
}

