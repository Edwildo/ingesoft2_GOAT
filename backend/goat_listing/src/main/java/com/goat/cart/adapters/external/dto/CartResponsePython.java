package com.goat.cart.adapters.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para la respuesta del carrito desde el servicio Python.
 * Mapea la respuesta JSON de Python a objetos Java.
 */
public record CartResponsePython(
        String id,
        @JsonProperty("userId")
        String userId,
        List<CartItemResponsePython> items,
        BigDecimal total,
        @JsonProperty("updatedAt")
        LocalDateTime updatedAt
) {}

