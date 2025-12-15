package com.goat.order.domain.exceptions;

/**
 * Excepción lanzada cuando el pago falla.
 */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
