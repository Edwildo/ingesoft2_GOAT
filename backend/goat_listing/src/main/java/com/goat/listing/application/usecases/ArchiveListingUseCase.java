package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.ports.ListingRepository;

import java.util.UUID;

/**
 * Caso de uso para archivar un listing (PUBLISHED -> ARCHIVED).
 */
public class ArchiveListingUseCase {
    private final ListingRepository listingRepository;

    public ArchiveListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Archiva un listing cambiando su estado de PUBLISHED a ARCHIVED.
     * Valida que el listing pertenezca al seller y esté en estado PUBLISHED.
     *
     * @param listingId ID del listing a archivar
     * @param sellerId ID del seller que archiva (para validar ownership)
     * @return Listing archivado
     * @throws IllegalArgumentException si el listing no existe
     * @throws IllegalStateException si el listing no puede ser archivado
     */
    public ListingResponse execute(UUID listingId, UUID sellerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing no encontrado"));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("No tienes permiso para archivar este listing");
        }

        listing.archive();
        Listing archivedListing = listingRepository.save(listing);

        return convertToResponse(archivedListing);
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
