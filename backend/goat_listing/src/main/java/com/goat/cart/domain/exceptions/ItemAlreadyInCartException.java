package com.goat.cart.domain.exceptions;

/**
 * Excepción lanzada cuando se intenta agregar un item que ya existe en el carrito.
 */
public class ItemAlreadyInCartException extends RuntimeException {
    public ItemAlreadyInCartException(String message) {
        super(message);
    }

    public ItemAlreadyInCartException(String message, Throwable cause) {
        super(message, cause);
    }
}
