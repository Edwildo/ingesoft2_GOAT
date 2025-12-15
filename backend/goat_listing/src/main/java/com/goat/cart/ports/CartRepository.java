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
}
