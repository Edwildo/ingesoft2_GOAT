package com.goat.order.adapters.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase embebida para mapear shipping_address como JSONB en PostgreSQL.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressEntity {
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}

