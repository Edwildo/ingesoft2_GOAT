package com.goat.listing.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para la solicitud de actualización de un listing.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateListingRequest {
    @NotBlank(message = "El SKU del sneaker es requerido")
    @Size(max = 64, message = "El SKU no debe superar 64 caracteres")
    private String sneakerSku;

    @NotBlank(message = "La talla es requerida")
    @Size(max = 16, message = "La talla no debe superar 16 caracteres")
    private String size;

    @NotBlank(message = "La condición es requerida")
    @Size(max = 32, message = "La condición no debe superar 32 caracteres")
    private String condition;

    @NotBlank(message = "El género es requerido")
    @Size(max = 16, message = "El género no debe superar 16 caracteres")
    private String gender;

    @NotBlank(message = "La marca es requerida")
    @Size(max = 64, message = "La marca no debe superar 64 caracteres")
    private String brand;

    @NotBlank(message = "El color es requerido")
    @Size(max = 32, message = "El color no debe superar 32 caracteres")
    private String color;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    private String coverImage;
}
