package com.goat.listing.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO para la solicitud de actualización de un listing.
 * El sneakerSku es opcional porque normalmente no se cambia durante la actualización.
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateListingRequest {
    // sneakerSku es opcional - si no se envía, se mantiene el valor original
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
