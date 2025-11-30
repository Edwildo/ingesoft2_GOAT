package com.goat.listing.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para la solicitud de creación de un sneaker en el catálogo.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateSneakerRequest {
    @NotBlank(message = "El SKU es requerido")
    @Size(min = 3, max = 50, message = "El SKU debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "El SKU solo puede contener letras mayúsculas, números, guiones y guiones bajos")
    private String sku;

    @NotBlank(message = "La marca es requerida")
    private String brand;

    @NotBlank(message = "El modelo es requerido")
    private String model;

    @NotBlank(message = "El género es requerido")
    @Pattern(regexp = "^(MALE|FEMALE|UNISEX)$", message = "El género debe ser MALE, FEMALE o UNISEX")
    private String gender;

    private String description;
    private List<String> categories;
    private List<String> collections;
    private SneakerMediaRequest media;

    /**
     * DTO interno para información de medios.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class SneakerMediaRequest {
        private String coverImage;
        private List<String> gallery;
    }
}

