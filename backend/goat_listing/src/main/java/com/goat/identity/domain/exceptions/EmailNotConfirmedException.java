package com.goat.identity.domain.exceptions;

/**
 * Excepción de dominio lanzada cuando el email no está confirmado.
 */
public class EmailNotConfirmedException extends RuntimeException {
    public EmailNotConfirmedException(String message) {
        super(message);
    }
}

