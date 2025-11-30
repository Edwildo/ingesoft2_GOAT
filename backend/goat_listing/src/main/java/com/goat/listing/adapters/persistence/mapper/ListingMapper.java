package com.goat.listing.adapters.persistence.mapper;

import com.goat.listing.adapters.persistence.entity.ListingEntity;
import com.goat.listing.domain.entities.Listing;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio y entidades JPA de Listing.
 */
public class ListingMapper {
    /**
     * Convierte una entidad JPA a una entidad de dominio.
     */
    public static Listing toDomain(ListingEntity entity) {
        if (entity == null) {
            return null;
        }

        Listing listing = new Listing(
                entity.getSellerId(),
                entity.getSneakerSku(),
                entity.getSize(),
                entity.getCondition(),
                entity.getGender(),
                entity.getBrand(),
                entity.getColor(),
                entity.getPrice(),
                entity.getCoverImage()
        );
        
        listing.setId(entity.getId());
        listing.setStatus(entity.getStatus());
        listing.setCreatedAt(entity.getCreatedAt());
        listing.setUpdatedAt(entity.getUpdatedAt());

        return listing;
    }

    /**
     * Convierte una lista de entidades JPA a entidades de dominio.
     */
    public static List<Listing> toDomainList(List<ListingEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(ListingMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad de dominio a una entidad JPA.
     */
    public static ListingEntity toEntity(Listing listing) {
        if (listing == null) {
            return null;
        }

        ListingEntity entity = new ListingEntity();
        entity.setId(listing.getId());
        entity.setSellerId(listing.getSellerId());
        entity.setSneakerSku(listing.getSneakerSku());
        entity.setSize(listing.getSize());
        entity.setCondition(listing.getCondition());
        entity.setGender(listing.getGender());
        entity.setBrand(listing.getBrand());
        entity.setColor(listing.getColor());
        entity.setPrice(listing.getPrice());
        entity.setStatus(listing.getStatus());
        entity.setCoverImage(listing.getCoverImage());
        entity.setCreatedAt(listing.getCreatedAt());
        entity.setUpdatedAt(listing.getUpdatedAt());

        return entity;
    }
}
