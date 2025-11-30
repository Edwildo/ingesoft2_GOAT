package com.goat.listing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO inmutable para la respuesta de un listing.
 */
public record ListingResponse(
    UUID id,
    UUID sellerId,
    String sneakerSku,
    String size,
    String condition,
    String gender,
    String brand,
    String color,
    BigDecimal price,
    String status,
    String coverImage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
