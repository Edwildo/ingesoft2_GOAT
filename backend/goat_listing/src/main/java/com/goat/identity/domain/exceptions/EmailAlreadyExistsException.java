package com.goat.identity.domain.exceptions;

/**
 * Excepción de dominio lanzada cuando se intenta crear un usuario con un email que ya existe.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}

