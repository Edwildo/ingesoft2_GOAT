package com.goat.cart.domain.exceptions;

/**
 * Excepción lanzada cuando no se encuentra un item en el carrito.
 */
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
