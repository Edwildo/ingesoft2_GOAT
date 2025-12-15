package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.domain.enums.ListingStatus;
import com.goat.listing.ports.ListingRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso para obtener un listing por su ID.
 * Para acceso público: solo retorna listings PUBLISHED.
 * Para sellers: pueden obtener sus propios listings en cualquier estado.
 */
public class GetListingUseCase {
    private final ListingRepository listingRepository;

    public GetListingUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Obtiene un listing por su ID (acceso público).
     * Solo retorna listings en estado PUBLISHED.
     *
     * @param listingId ID del listing a obtener
     * @return Listing encontrado
     * @throws IllegalArgumentException si el listing no existe o no está publicado
     */
    public ListingResponse execute(UUID listingId) {
        return execute(listingId, null);
    }

    /**
     * Obtiene un listing por su ID.
     * Si se proporciona sellerId y es el dueño del listing, puede obtenerlo en cualquier estado.
     * Si no se proporciona sellerId o no es el dueño, solo puede obtener listings PUBLISHED.
     *
     * @param listingId ID del listing a obtener
     * @param sellerId ID del seller (opcional, para verificar ownership)
     * @return Listing encontrado
     * @throws IllegalArgumentException si el listing no existe o no está disponible
     */
    public ListingResponse execute(UUID listingId, UUID sellerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing no encontrado"));

        // Si el sellerId es proporcionado y es el dueño, puede obtener el listing en cualquier estado
        boolean isOwner = sellerId != null && listing.getSellerId().equals(sellerId);
        
        // Si no es el dueño, solo puede obtener listings PUBLISHED
        if (!isOwner && listing.getStatus() != ListingStatus.PUBLISHED) {
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
