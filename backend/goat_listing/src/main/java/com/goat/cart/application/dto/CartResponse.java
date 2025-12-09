package com.goat.cart.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para la respuesta del carrito.
 */
public record CartResponse(
        UUID cartId,
        UUID userId,
        String status,
        List<CartItemResponse> items,
        int totalItems,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
