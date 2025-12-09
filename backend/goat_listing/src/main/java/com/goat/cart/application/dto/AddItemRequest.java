package com.goat.cart.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * DTO para agregar un item al carrito.
 */
public record AddItemRequest(
        @NotNull(message = "listingId no puede ser nulo")
        UUID listingId
) {}
