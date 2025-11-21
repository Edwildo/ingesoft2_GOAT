package com.goat.identity.domain.exceptions;

/**
 * Excepción de dominio lanzada cuando las credenciales son inválidas.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}

