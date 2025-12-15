package com.goat.order.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Value object que representa una dirección de envío.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    /**
     * Valida que la dirección tenga todos los campos requeridos.
     */
    public boolean isValid() {
        return street != null && !street.trim().isEmpty()
                && city != null && !city.trim().isEmpty()
                && state != null && !state.trim().isEmpty()
                && postalCode != null && !postalCode.trim().isEmpty()
                && country != null && !country.trim().isEmpty();
    }
}

