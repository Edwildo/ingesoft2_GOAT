package com.goat.order.domain.exceptions;

/**
 * Excepción lanzada cuando se intenta crear una orden con un carrito vacío.
 */
public class EmptyCartException extends RuntimeException {
    public EmptyCartException(String message) {
        super(message);
    }
}

