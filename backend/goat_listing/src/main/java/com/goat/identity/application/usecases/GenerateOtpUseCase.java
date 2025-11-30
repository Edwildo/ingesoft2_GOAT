package com.goat.identity.application.usecases;

import com.goat.identity.application.dto.GenerateOtpRequest;
import com.goat.identity.application.dto.GenerateOtpResponse;
import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.ports.OtpGenerationPort;

/**
 * Caso de uso para generar un código OTP.
 * Delega la generación al servicio Python.
 */
public class GenerateOtpUseCase {
    private final OtpGenerationPort otpGenerationPort;

    public GenerateOtpUseCase(OtpGenerationPort otpGenerationPort) {
        this.otpGenerationPort = otpGenerationPort;
    }

    public GenerateOtpResponse execute(GenerateOtpRequest request) {
        Email email = Email.of(request.email());
        boolean success = otpGenerationPort.generateOtp(email, request.purpose());

        if (success) {
            return new GenerateOtpResponse(
                    true,
                    "OTP generado correctamente",
                    5 // Por defecto 5 minutos según la documentación
            );
        } else {
            return new GenerateOtpResponse(
                    false,
                    "Error al generar el OTP. Verifique que el servicio Python esté disponible.",
                    null
            );
        }
    }
}

