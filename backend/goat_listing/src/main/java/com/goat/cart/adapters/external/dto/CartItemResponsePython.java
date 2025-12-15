package com.goat.cart.adapters.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para un item del carrito desde el servicio Python.
 * Mapea la respuesta JSON de Python a objetos Java.
 */
public record CartItemResponsePython(
        String id,
        @JsonProperty("listingId")
        String listingId,
        @JsonProperty("sneakerSku")
        String sneakerSku,
        String size,
        BigDecimal price,
        String brand,
        String color,
        String condition,
        @JsonProperty("coverImage")
        String coverImage,
        @JsonProperty("createdAt")
        LocalDateTime createdAt
) {}

