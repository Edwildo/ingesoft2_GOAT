package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.application.dto.UpdateListingRequest;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.ports.ListingRepository;

import java.util.UUID;

/**
 * Caso de uso para actualizar un listing existente.
 * Permite actualizar listings en estado DRAFT o ARCHIVED.
 * Si el listing está ARCHIVED, se cambia automáticamente a DRAFT.
 */
public class UpdateListingUseCase {
    private final ListingRepository listingRepository;

    public UpdateListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Actualiza un listing existente.
     * Valida que el listing esté en estado DRAFT o ARCHIVED y pertenezca al seller.
     * Si está ARCHIVED, se cambia automáticamente a DRAFT.
     *
     * @param listingId ID del listing a actualizar
     * @param sellerId ID del seller que actualiza (para validar ownership)
     * @param request Datos actualizados del listing
     * @return Listing actualizado
     * @throws IllegalArgumentException si el listing no existe
     * @throws IllegalStateException si el listing no puede ser editado (no está en DRAFT o no pertenece al seller)
     */
    public ListingResponse execute(UUID listingId, UUID sellerId, UpdateListingRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing no encontrado"));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("No tienes permiso para editar este listing");
        }

        if (!listing.canBeEdited()) {
            throw new IllegalStateException("Solo listings en estado DRAFT o ARCHIVED pueden editarse");
        }

        // Si el listing está ARCHIVED, cambiarlo a DRAFT para permitir edición
        if (listing.getStatus() == com.goat.listing.domain.enums.ListingStatus.ARCHIVED) {
            listing.setStatus(com.goat.listing.domain.enums.ListingStatus.DRAFT);
        }

        // Solo actualizar sneakerSku si se proporciona (normalmente no se cambia)
        if (request.getSneakerSku() != null && !request.getSneakerSku().isBlank()) {
            listing.setSneakerSku(request.getSneakerSku());
        }
        listing.setSize(request.getSize());
        listing.setCondition(request.getCondition());
        listing.setGender(request.getGender());
        listing.setBrand(request.getBrand());
        listing.setColor(request.getColor());
        listing.setPrice(request.getPrice());
        listing.setCoverImage(request.getCoverImage());
        listing.setUpdatedAt(java.time.LocalDateTime.now());

        Listing updatedListing = listingRepository.save(listing);

        return convertToResponse(updatedListing);
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
