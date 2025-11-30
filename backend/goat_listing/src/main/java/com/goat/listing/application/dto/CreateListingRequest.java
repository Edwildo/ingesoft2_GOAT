package com.goat.listing.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para la solicitud de creación de un listing.
 * Necesita getters/setters para deserialización JSON y acceso a datos.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateListingRequest {
    @NotBlank(message = "El SKU del sneaker es requerido")
    private String sneakerSku;

    @NotBlank(message = "La talla es requerida")
    private String size;

    @NotBlank(message = "La condición es requerida")
    private String condition;

    @NotBlank(message = "El género es requerido")
    private String gender;

    @NotBlank(message = "La marca es requerida")
    private String brand;

    @NotBlank(message = "El color es requerido")
    private String color;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    private String coverImage;
}
