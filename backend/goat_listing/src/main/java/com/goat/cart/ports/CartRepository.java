package com.goat.cart.ports;

import com.goat.cart.domain.entities.Cart;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (interfaz) para acceder al repositorio de carritos.
 * Define el contrato que debe cumplir la implementación de persistencia.
 */
public interface CartRepository {

    /**
     * Busca el carrito ACTIVE de un usuario.
     * @param userId ID del usuario
     * @return Optional con el carrito si existe, empty si no
     */
    Optional<Cart> findActiveByUser(UUID userId);

    /**
     * Guarda o actualiza un carrito en la base de datos.
     * @param cart el carrito a guardar
     */
    void save(Cart cart);

    /**
     * Elimina un item del carrito.
     * @param cartId ID del carrito
     * @param itemId ID del item
     */
    void removeItem(UUID cartId, UUID itemId);

    /**
     * Elimina todos los items del carrito (lo vacía).
     * @param cartId ID del carrito
     */
    void clearCart(UUID cartId);
}
