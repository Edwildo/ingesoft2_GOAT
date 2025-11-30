package com.goat.identity.application.dto;

/**
 * DTO inmutable para la respuesta de generación de OTP exitosa.
 */
public record GenerateOtpResponse(
    boolean success,
    String message,
    Integer expiresInMinutes
) {}

