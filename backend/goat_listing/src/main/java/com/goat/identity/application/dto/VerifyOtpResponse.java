package com.goat.identity.application.dto;

/**
 * DTO para la respuesta de verificación de OTP.
 */
public class VerifyOtpResponse {
    private boolean success;
    private String message;
    private boolean valid;

    public VerifyOtpResponse() {
    }

    public VerifyOtpResponse(boolean success, String message, boolean valid) {
        this.success = success;
        this.message = message;
        this.valid = valid;
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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}

