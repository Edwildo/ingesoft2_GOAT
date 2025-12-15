package com.goat.order.domain.enums;

/**
 * Estados posibles de una orden.
 * 
 * PENDING: Orden creada, esperando procesamiento de pago
 * PAYMENT_PENDING: Pago en proceso (mock)
 * CONFIRMED: Pago confirmado, orden confirmada
 * SHIPPING: Orden en envío
 * DELIVERED: Orden entregada
 * CANCELLED: Orden cancelada
 */
public enum OrderStatus {
    PENDING,
    PAYMENT_PENDING,
    CONFIRMED,
    SHIPPING,
    DELIVERED,
    CANCELLED
}

