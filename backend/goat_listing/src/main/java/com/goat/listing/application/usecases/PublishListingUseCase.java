package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.ports.ListingRepository;

import java.util.UUID;

/**
 * Caso de uso para publicar un listing (DRAFT -> PUBLISHED o ARCHIVED -> PUBLISHED).
 */
public class PublishListingUseCase {
    private final ListingRepository listingRepository;

    public PublishListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Publica un listing cambiando su estado de DRAFT o ARCHIVED a PUBLISHED.
     * Valida que el listing pertenezca al seller y esté en estado DRAFT o ARCHIVED.
     *
     * @param listingId ID del listing a publicar
     * @param sellerId ID del seller que publica (para validar ownership)
     * @return Listing publicado
     * @throws IllegalArgumentException si el listing no existe
     * @throws IllegalStateException si el listing no puede ser publicado
     */
    public ListingResponse execute(UUID listingId, UUID sellerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing no encontrado"));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("No tienes permiso para publicar este listing");
        }

        listing.publish();
        Listing publishedListing = listingRepository.save(listing);

        return convertToResponse(publishedListing);
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
