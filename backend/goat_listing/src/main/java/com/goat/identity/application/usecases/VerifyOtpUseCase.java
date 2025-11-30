package com.goat.identity.application.usecases;

import com.goat.identity.application.dto.VerifyOtpRequest;
import com.goat.identity.application.dto.VerifyOtpResponse;
import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.ports.OtpValidationPort;
import com.goat.identity.ports.UserRepository;

/**
 * Caso de uso para verificar un código OTP.
 * Si el propósito es EMAIL_CONFIRMATION y el OTP es válido,
 * actualiza el estado del usuario en la base de datos.
 */
public class VerifyOtpUseCase {
    private final OtpValidationPort otpValidationPort;
    private final UserRepository userRepository;

    public VerifyOtpUseCase(
            OtpValidationPort otpValidationPort,
            UserRepository userRepository) {
        this.otpValidationPort = otpValidationPort;
        this.userRepository = userRepository;
    }

    public VerifyOtpResponse execute(VerifyOtpRequest request) {
        Email email = Email.of(request.email());
        boolean isValid = otpValidationPort.validateOtp(email, request.otp(), request.purpose());

        if (isValid) {
            if ("EMAIL_CONFIRMATION".equals(request.purpose())) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    if (!user.getEmailConfirmed()) {
                        user.setEmailConfirmed(true);
                        userRepository.save(user);
                    }
                });
            }

            return new VerifyOtpResponse(
                    true,
                    "OTP válido",
                    true
            );
        } else {
            return new VerifyOtpResponse(
                    false,
                    "OTP inválido o expirado",
                    false
            );
        }
    }
}

