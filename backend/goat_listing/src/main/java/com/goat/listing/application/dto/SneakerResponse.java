package com.goat.listing.application.dto;

import java.util.List;

/**
 * DTO para la respuesta de un sneaker del catálogo.
 * Representa la información completa de un sneaker desde el servicio Python.
 */
public record SneakerResponse(
        String sku,
        String brand,
        String model,
        String gender,
        String description,
        List<String> categories,
        List<String> collections,
        SneakerMedia media
) {
    /**
     * DTO interno para información de medios del sneaker.
     */
    public record SneakerMedia(
            String coverImage,
            List<String> gallery
    ) {}
}

