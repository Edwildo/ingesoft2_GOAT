package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.CreateListingRequest;
import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.ports.CatalogServicePort;
import com.goat.listing.ports.ListingRepository;

import java.util.UUID;

/**
 * Caso de uso para crear un nuevo listing.
 * Valida el SKU con el servicio Python antes de crear.
 */
public class CreateListingUseCase {
    private final ListingRepository listingRepository;
    private final CatalogServicePort catalogServicePort;

    public CreateListingUseCase(
            ListingRepository listingRepository,
            CatalogServicePort catalogServicePort) {
        this.listingRepository = listingRepository;
        this.catalogServicePort = catalogServicePort;
    }

    /**
     * Crea un nuevo listing en estado DRAFT.
     * Valida que el SKU exista en el catálogo de Python.
     *
     * @param sellerId ID del seller que crea el listing
     * @param request Datos del listing a crear
     * @return Listing creado
     * @throws IllegalArgumentException si el SKU no existe en el catálogo
     */
    public ListingResponse execute(UUID sellerId, CreateListingRequest request) {
        // Validar que el SKU existe en el catálogo de Python
        boolean skuExists = catalogServicePort.validateSneakerSku(request.getSneakerSku());
        if (!skuExists) {
            throw new IllegalArgumentException("El SKU del sneaker no existe en el catálogo");
        }

        Listing listing = new Listing(
                sellerId,
                request.getSneakerSku(),
                request.getSize(),
                request.getCondition(),
                request.getGender(),
                request.getBrand(),
                request.getColor(),
                request.getPrice(),
                request.getCoverImage()
        );

        Listing savedListing = listingRepository.save(listing);

        return convertToResponse(savedListing);
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
