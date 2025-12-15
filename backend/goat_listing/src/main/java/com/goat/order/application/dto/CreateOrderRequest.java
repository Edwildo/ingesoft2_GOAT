package com.goat.order.application.dto;

import com.goat.order.domain.entities.ShippingAddress;
import com.goat.order.domain.enums.ShippingMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para crear una orden desde el carrito.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private ShippingAddressDto shippingAddress;
    private ShippingMethod shippingMethod;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public ShippingAddress toDomain() {
            ShippingAddress address = new ShippingAddress();
            address.setStreet(street);
            address.setCity(city);
            address.setState(state);
            address.setPostalCode(postalCode);
            address.setCountry(country);
            return address;
        }
    }
}

