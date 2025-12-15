package com.goat.cart.application.usecases;

import com.goat.cart.adapters.external.RestCartServiceAdapter;
import com.goat.cart.adapters.external.dto.AddItemRequestPython;
import com.goat.cart.domain.exceptions.CannotAddOwnListingException;
import com.goat.cart.domain.exceptions.ItemAlreadyInCartException;
import com.goat.cart.domain.exceptions.ListingNotAvailableException;
import com.goat.cart.ports.CartRepository;
import com.goat.listing.domain.enums.ListingStatus;
import com.goat.listing.ports.ListingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Use case para agregar un item al carrito.
 * 
 * Valida el listing en Java y luego delega al servicio Python para persistir en MongoDB.
 */
@Component
public class AddItemToCartUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AddItemToCartUseCase.class);

    private final CartRepository cartRepository;
    private final ListingRepository listingRepository;
    private final RestCartServiceAdapter cartServiceAdapter;

    public AddItemToCartUseCase(
            CartRepository cartRepository,
            ListingRepository listingRepository,
            RestCartServiceAdapter cartServiceAdapter) {
        this.cartRepository = cartRepository;
        this.listingRepository = listingRepository;
        this.cartServiceAdapter = cartServiceAdapter;
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

        // 3. Verificar que el item no esté ya en el carrito
        var cart = cartRepository.findActiveByUser(userId);
        if (cart.isPresent() && cart.get().containsItem(listingId)) {
            logger.warn("El item {} ya existe en el carrito del usuario {}", listingId, userId);
            throw new ItemAlreadyInCartException("Este item ya está en tu carrito");
        }

        // 4. Crear request con toda la información del listing
        var request = AddItemRequestPython.from(
                listingId,
                listing.getSneakerSku(),
                listing.getSize(),
                listing.getPrice(),
                listing.getBrand(),
                listing.getColor(),
                listing.getCondition(),
                listing.getCoverImage()
        );

        // 5. Agregar al carrito en el servicio Python
        cartServiceAdapter.addItem(userId, request);

        logger.info("Item {} agregado al carrito del usuario {}", listingId, userId);
    }
}
