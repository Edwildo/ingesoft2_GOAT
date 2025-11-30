package com.goat.identity.application.dto;

/**
 * DTO inmutable para la respuesta de verificación de OTP.
 */
public record VerifyOtpResponse(
    boolean success,
    String message,
    boolean valid
) {}

