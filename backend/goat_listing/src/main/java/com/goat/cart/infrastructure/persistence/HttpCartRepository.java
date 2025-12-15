package com.goat.cart.infrastructure.persistence;

import com.goat.cart.adapters.external.RestCartServiceAdapter;
import com.goat.cart.domain.entities.Cart;
import com.goat.cart.ports.CartRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del puerto CartRepository usando HTTP para comunicarse
 * con el servicio Python que gestiona el carrito en MongoDB.
 * 
 * Esta implementación reemplaza PostgreSQLCartRepository y mantiene
 * la arquitectura hexagonal: el dominio y casos de uso no cambian,
 * solo cambia la implementación de infraestructura.
 * 
 * Los casos de uso usan el adaptador directamente para operaciones
 * que requieren userId (addItem, removeItem, clearCart).
 */
@Component
public class HttpCartRepository implements CartRepository {

    private final RestCartServiceAdapter cartServiceAdapter;

    public HttpCartRepository(RestCartServiceAdapter cartServiceAdapter) {
        this.cartServiceAdapter = cartServiceAdapter;
    }

    @Override
    public Optional<Cart> findActiveByUser(UUID userId) {
        return cartServiceAdapter.findActiveByUser(userId);
    }
}

