package com.goat.cart.application.usecases;

import com.goat.cart.domain.entities.Cart;
import com.goat.cart.ports.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case para obtener el carrito de un usuario.
 */
@Component
public class GetCartUseCase {

    private static final Logger logger = LoggerFactory.getLogger(GetCartUseCase.class);

    private final CartRepository cartRepository;

    public GetCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart execute(UUID userId) {
        logger.info("Obteniendo carrito del usuario {}", userId);

        // Buscar carrito ACTIVE o crear uno vacío si no existe
        return cartRepository.findActiveByUser(userId)
                .orElseGet(() -> Cart.createNew(userId));
    }
}
