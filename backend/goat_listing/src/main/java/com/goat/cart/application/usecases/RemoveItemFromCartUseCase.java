package com.goat.cart.application.usecases;

import com.goat.cart.domain.exceptions.ItemNotFoundException;
import com.goat.cart.ports.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case para remover un item del carrito.
 */
@Component
public class RemoveItemFromCartUseCase {

    private static final Logger logger = LoggerFactory.getLogger(RemoveItemFromCartUseCase.class);

    private final CartRepository cartRepository;

    public RemoveItemFromCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void execute(UUID userId, UUID itemId) {
        logger.info("Removiendo item {} del carrito del usuario {}", itemId, userId);

        // 1. Obtener carrito del usuario
        var cart = cartRepository.findActiveByUser(userId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        // 2. Verificar que el item existe en el carrito
        var itemExists = cart.getItems().stream()
                .anyMatch(i -> i.getId().equals(itemId));

        if (!itemExists) {
            throw new ItemNotFoundException("El item no se encuentra en el carrito");
        }

        // 3. Remover el item
        cart.removeItem(itemId);

        // 4. Guardar los cambios
        cartRepository.save(cart);

        logger.info("Item {} removido del carrito del usuario {}", itemId, userId);
    }
}
