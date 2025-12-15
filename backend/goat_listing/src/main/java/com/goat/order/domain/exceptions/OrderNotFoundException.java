package com.goat.order.domain.exceptions;

import java.util.UUID;

/**
 * Excepción lanzada cuando una orden no se encuentra.
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID orderId) {
        super("Orden no encontrada: " + orderId);
    }
}
