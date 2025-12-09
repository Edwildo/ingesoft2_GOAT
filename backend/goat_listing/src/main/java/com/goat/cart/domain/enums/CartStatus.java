package com.goat.cart.domain.enums;

/**
 * Estados posibles del carrito.
 */
public enum CartStatus {
    ACTIVE,      // Carrito activo, siendo editado
    CHECKOUT,    // En proceso de compra
    ABANDONED    // Abandonado (sin compra)
}
