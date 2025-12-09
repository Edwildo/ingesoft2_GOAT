package com.goat.cart.domain.exceptions;

/**
 * Excepción lanzada cuando el listing no está disponible (no es PUBLISHED).
 */
public class ListingNotAvailableException extends RuntimeException {
    public ListingNotAvailableException(String message) {
        super(message);
    }

    public ListingNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
