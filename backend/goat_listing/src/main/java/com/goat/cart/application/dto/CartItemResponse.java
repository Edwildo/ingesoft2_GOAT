package com.goat.cart.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para representar un item del carrito en la respuesta.
 */
public record CartItemResponse(
        UUID itemId,
        UUID listingId,
        String sneakerSku,
        String size,
        BigDecimal price,
        String brand,
        String color,
        String condition,
        String coverImage,
        LocalDateTime addedAt
) {}
