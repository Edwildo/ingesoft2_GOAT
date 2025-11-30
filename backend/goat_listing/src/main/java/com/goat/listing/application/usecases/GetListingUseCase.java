package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.domain.enums.ListingStatus;
import com.goat.listing.ports.ListingRepository;

import java.util.UUID;

/**
 * Caso de uso para obtener un listing por su ID.
 * Solo retorna listings PUBLICADOS para acceso público.
 */
public class GetListingUseCase {
    private final ListingRepository listingRepository;

    public GetListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Obtiene un listing por su ID.
     * Solo retorna listings en estado PUBLISHED para acceso público.
     *
     * @param listingId ID del listing a obtener
     * @return Listing encontrado
     * @throws IllegalArgumentException si el listing no existe o no está publicado
     */
    public ListingResponse execute(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing no encontrado"));

        if (listing.getStatus() != ListingStatus.PUBLISHED) {
            throw new IllegalArgumentException("Listing no disponible");
        }

        return convertToResponse(listing);
    }

    private ListingResponse convertToResponse(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getSellerId(),
                listing.getSneakerSku(),
                listing.getSize(),
                listing.getCondition(),
                listing.getGender(),
                listing.getBrand(),
                listing.getColor(),
                listing.getPrice(),
                listing.getStatus().getCode(),
                listing.getCoverImage(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}
