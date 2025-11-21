package com.goat.identity.ports;

import com.goat.identity.domain.valueobjects.Email;

/**
 * Puerto (interfaz) para verificar el estado de verificación de OTP/Email.
 * Define la operación de verificación sin depender de implementación.
 */
public interface OtpVerificationPort {
    /**
     * Verifica si el email del usuario ha sido confirmado (OTP verificado).
     *
     * @param email Email del usuario a verificar
     * @return true si el email está confirmado, false en caso contrario
     */
    boolean isEmailConfirmed(Email email);
}

