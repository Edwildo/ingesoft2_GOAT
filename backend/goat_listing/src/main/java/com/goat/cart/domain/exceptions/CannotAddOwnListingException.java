package com.goat.cart.domain.exceptions;

/**
 * Excepción lanzada cuando un usuario intenta agregar su propio listing al carrito.
 */
public class CannotAddOwnListingException extends RuntimeException {
    public CannotAddOwnListingException(String message) {
        super(message);
    }

    public CannotAddOwnListingException(String message, Throwable cause) {
        super(message, cause);
    }
}
