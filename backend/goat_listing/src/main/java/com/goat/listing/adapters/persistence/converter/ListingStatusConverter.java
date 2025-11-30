package com.goat.listing.adapters.persistence.converter;

import com.goat.listing.domain.enums.ListingStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter para mapear ListingStatus enum a PostgreSQL ENUM type.
 * Convierte entre el enum Java y el tipo ENUM de PostgreSQL (listing.listing_status).
 * 
 * Nota: Este converter retorna el código del enum como String, pero PostgreSQL
 * requiere un cast explícito. La solución es usar @Column con columnDefinition
 * que especifique el tipo ENUM, y Hibernate debería manejar el cast automáticamente.
 */
@Converter(autoApply = false)
public class ListingStatusConverter implements AttributeConverter<ListingStatus, String> {

    @Override
    public String convertToDatabaseColumn(ListingStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public ListingStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ListingStatus.fromCode(dbData);
    }
}

