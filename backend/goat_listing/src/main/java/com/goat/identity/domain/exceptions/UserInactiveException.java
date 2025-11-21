package com.goat.identity.domain.exceptions;

/**
 * Excepción de dominio lanzada cuando el usuario está inactivo.
 */
public class UserInactiveException extends RuntimeException {
    public UserInactiveException(String message) {
        super(message);
    }
}

