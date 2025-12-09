package com.goat.cart.infrastructure.persistence.converter;

import com.goat.cart.domain.enums.CartStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Conversor JPA para CartStatus enum.
 * Mapea entre enum y string en la BD.
 */
@Converter(autoApply = true)
public class CartStatusConverter implements AttributeConverter<CartStatus, String> {

    @Override
    public String convertToDatabaseColumn(CartStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public CartStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return CartStatus.valueOf(dbData);
    }
}
