package com.goat.identity.application.dto;

/**
 * DTO para la respuesta de generación de OTP exitosa.
 */
public class GenerateOtpResponse {
    private boolean success;
    private String message;
    private Integer expiresInMinutes;

    public GenerateOtpResponse() {
    }

    public GenerateOtpResponse(boolean success, String message, Integer expiresInMinutes) {
        this.success = success;
        this.message = message;
        this.expiresInMinutes = expiresInMinutes;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public void setExpiresInMinutes(Integer expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }
}

