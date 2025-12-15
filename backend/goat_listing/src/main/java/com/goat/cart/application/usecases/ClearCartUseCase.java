package com.goat.cart.application.usecases;

import com.goat.cart.adapters.external.RestCartServiceAdapter;
import com.goat.cart.ports.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case para vaciar el carrito de un usuario.
 * 
 * Usa el adaptador HTTP directamente para comunicarse con el servicio Python,
 * manteniendo la arquitectura hexagonal (el adaptador está en la capa de infraestructura).
 */
@Component
public class ClearCartUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ClearCartUseCase.class);

    private final CartRepository cartRepository;
    private final RestCartServiceAdapter cartServiceAdapter;

    public ClearCartUseCase(CartRepository cartRepository, RestCartServiceAdapter cartServiceAdapter) {
        this.cartRepository = cartRepository;
        this.cartServiceAdapter = cartServiceAdapter;
    }

    public void execute(UUID userId) {
        logger.info("Vaciando carrito del usuario {}", userId);

        // 1. Verificar que el carrito existe
        var cart = cartRepository.findActiveByUser(userId);

        if (cart.isEmpty()) {
            logger.info("El carrito del usuario {} ya estaba vacío", userId);
            return;
        }

        // 2. Vaciar el carrito directamente en el servicio Python
        cartServiceAdapter.clearCart(userId);

        logger.info("Carrito del usuario {} vaciado", userId);
    }
}
