package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.ListingListResponse;
import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.domain.enums.ListingStatus;
import com.goat.listing.ports.ListingRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para obtener listings del seller autenticado.
 */
public class GetMyListingsUseCase {
    private final ListingRepository listingRepository;

    public GetMyListingsUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Obtiene listings del seller con filtro opcional por estado y paginación.
     *
     * @param sellerId ID del seller
     * @param status Filtro por estado (opcional)
     * @param page Número de página (base 0)
     * @param pageSize Tamaño de página
     * @return Respuesta con listings y metadatos de paginación
     */
    public ListingListResponse execute(UUID sellerId, ListingStatus status, int page, int pageSize) {
        List<Listing> listings = listingRepository.findBySellerId(sellerId, status, page, pageSize);
        
        long total = listingRepository.countBySellerId(sellerId, status);
        
        List<ListingResponse> listingResponses = listings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new ListingListResponse(listingResponses, total, page, pageSize);
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
