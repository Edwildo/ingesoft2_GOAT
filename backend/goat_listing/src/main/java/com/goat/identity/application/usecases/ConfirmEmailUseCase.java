package com.goat.identity.application.usecases;

import com.goat.identity.domain.entities.User;
import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.ports.OtpVerificationPort;
import com.goat.identity.ports.UserRepository;

/**
 * Caso de uso para confirmar el email de un usuario.
 * Verifica con el servicio Python si el OTP fue verificado y actualiza el estado.
 */
public class ConfirmEmailUseCase {
    private final UserRepository userRepository;
    private final OtpVerificationPort otpVerificationPort;

    public ConfirmEmailUseCase(UserRepository userRepository, OtpVerificationPort otpVerificationPort) {
        this.userRepository = userRepository;
        this.otpVerificationPort = otpVerificationPort;
    }

    /**
     * Verifica con el servicio Python si el email está confirmado y actualiza el estado en la BD.
     *
     * @param email Email del usuario a verificar
     * @return true si el email fue confirmado y actualizado, false en caso contrario
     */
    public boolean execute(Email email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Consultar al servicio Python si el OTP fue verificado
        boolean isConfirmed = otpVerificationPort.isEmailConfirmed(email);

        // Si el servicio Python confirma que el OTP fue verificado,
        // actualizar el estado en la base de datos PostgreSQL
        if (isConfirmed) {
            if (!user.getEmailConfirmed()) {
                // Actualizar el estado del usuario en la base de datos
                user.setEmailConfirmed(true);
                userRepository.save(user);
            }
            return true;
        }

        return false;
    }
}

