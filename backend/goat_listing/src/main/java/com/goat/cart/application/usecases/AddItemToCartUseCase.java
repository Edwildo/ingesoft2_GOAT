package com.goat.cart.application.usecases;

import com.goat.cart.domain.entities.Cart;
import com.goat.cart.domain.entities.CartItem;
import com.goat.cart.domain.exceptions.CannotAddOwnListingException;
import com.goat.cart.domain.exceptions.ListingNotAvailableException;
import com.goat.cart.ports.CartRepository;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.domain.enums.ListingStatus;
import com.goat.listing.ports.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case para agregar un item al carrito.
 */
@Component
public class AddItemToCartUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AddItemToCartUseCase.class);

    private final CartRepository cartRepository;
    private final ListingRepository listingRepository;

    public AddItemToCartUseCase(CartRepository cartRepository, ListingRepository listingRepository) {
        this.cartRepository = cartRepository;
        this.listingRepository = listingRepository;
    }

    public void execute(UUID userId, UUID listingId) {
        logger.info("Agregando listing {} al carrito del usuario {}", listingId, userId);

        // 1. Verificar que el listing existe y está PUBLISHED
        var listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing no encontrado"));

        if (listing.getStatus() != ListingStatus.PUBLISHED) {
            throw new ListingNotAvailableException("El listing no está disponible para compra");
        }

        // 2. Evitar que el seller se compre su propio listing
        if (listing.getSellerId().equals(userId)) {
            throw new CannotAddOwnListingException("No puedes agregar tus propios listings al carrito");
        }

        // 3. Buscar o crear carrito ACTIVE
        var cart = cartRepository.findActiveByUser(userId)
                .orElseGet(() -> Cart.createNew(userId));

        // 4. Evitar duplicados
        if (cart.containsItem(listingId)) {
            logger.warn("El item {} ya existe en el carrito del usuario {}", listingId, userId);
            throw new RuntimeException("Este item ya está en tu carrito");
        }

        // 5. Crear CartItem con snapshot del precio y metadatos
        var cartItem = new CartItem(
                listingId,
                listing.getSneakerSku(),
                listing.getSize(),
                listing.getPrice(),
                listing.getBrand(),
                listing.getColor(),
                listing.getCondition(),
                listing.getCoverImage()
        );

        // 6. Agregar al carrito
        cart.addItem(cartItem);

        // 7. Guardar
        cartRepository.save(cart);

        logger.info("Item {} agregado al carrito del usuario {}", listingId, userId);
    }
}
