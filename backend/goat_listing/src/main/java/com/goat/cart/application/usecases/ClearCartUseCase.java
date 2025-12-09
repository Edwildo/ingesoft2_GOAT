package com.goat.cart.application.usecases;

import com.goat.cart.ports.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case para vaciar el carrito de un usuario.
 */
@Component
public class ClearCartUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ClearCartUseCase.class);

    private final CartRepository cartRepository;

    public ClearCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void execute(UUID userId) {
        logger.info("Vaciando carrito del usuario {}", userId);

        // 1. Obtener carrito del usuario
        var cart = cartRepository.findActiveByUser(userId);

        if (cart.isEmpty()) {
            logger.info("El carrito del usuario {} ya estaba vacío", userId);
            return;
        }

        // 2. Limpiar items
        cart.get().clear();

        // 3. Guardar cambios
        cartRepository.save(cart.get());

        logger.info("Carrito del usuario {} vaciado", userId);
    }
}
