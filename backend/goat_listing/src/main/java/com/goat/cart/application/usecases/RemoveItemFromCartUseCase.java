package com.goat.cart.application.usecases;

import com.goat.cart.adapters.external.RestCartServiceAdapter;
import com.goat.cart.domain.exceptions.ItemNotFoundException;
import com.goat.cart.ports.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case para remover un item del carrito.
 * 
 * Usa el adaptador HTTP directamente para comunicarse con el servicio Python
 */
@Component
public class RemoveItemFromCartUseCase {

    private static final Logger logger = LoggerFactory.getLogger(RemoveItemFromCartUseCase.class);

    private final CartRepository cartRepository;
    private final RestCartServiceAdapter cartServiceAdapter;

    public RemoveItemFromCartUseCase(CartRepository cartRepository, RestCartServiceAdapter cartServiceAdapter) {
        this.cartRepository = cartRepository;
        this.cartServiceAdapter = cartServiceAdapter;
    }

    public void execute(UUID userId, UUID itemId) {
        logger.info("Removiendo item {} del carrito del usuario {}", itemId, userId);

        // 1. Verificar que el carrito existe y contiene el item
        var cart = cartRepository.findActiveByUser(userId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado"));

        var itemExists = cart.getItems().stream()
                .anyMatch(i -> i.getId().equals(itemId));

        if (!itemExists) {
            throw new ItemNotFoundException("El item no se encuentra en el carrito");
        }

        // 2. Remover el item directamente en el servicio Python
        cartServiceAdapter.removeItem(userId, itemId);

        logger.info("Item {} removido del carrito del usuario {}", itemId, userId);
    }
}
