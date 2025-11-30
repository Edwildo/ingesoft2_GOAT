package com.goat.listing.domain.exceptions;

/**
 * Excepción de dominio que se lanza cuando un usuario no tiene el rol necesario
 * para realizar una operación.
 */
public class InsufficientRoleException extends RuntimeException {
    public InsufficientRoleException(String message) {
        super(message);
    }
}
