package com.goat.cart.domain.exceptions;

/**
 * Excepción lanzada cuando no se encuentra el carrito del usuario.
 */
public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }

    public CartNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
