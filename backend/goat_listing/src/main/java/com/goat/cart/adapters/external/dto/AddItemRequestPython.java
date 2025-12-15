package com.goat.cart.adapters.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para agregar un item al carrito en el servicio Python.
 * Mapea el request de Java al formato esperado por Python.
 */
public record AddItemRequestPython(
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
        String coverImage
) {
    public static AddItemRequestPython from(UUID listingId, String sneakerSku, String size,
                                            BigDecimal price, String brand, String color,
                                            String condition, String coverImage) {
        return new AddItemRequestPython(
                listingId.toString(),
                sneakerSku,
                size,
                price,
                brand,
                color,
                condition,
                coverImage
        );
    }
}

