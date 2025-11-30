package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.ListingListResponse;
import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.ports.ListingRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para obtener listings públicos con filtros y paginación.
 */
public class GetListingsUseCase {
    private final ListingRepository listingRepository;

    public GetListingsUseCase(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    /**
     * Obtiene listings públicos (PUBLISHED) con filtros opcionales y paginación.
     *
     * @param brand Filtro por marca (opcional)
     * @param size Filtro por talla (opcional)
     * @param condition Filtro por condición (opcional)
     * @param gender Filtro por género (opcional)
     * @param color Filtro por color (opcional)
     * @param minPrice Precio mínimo (opcional)
     * @param maxPrice Precio máximo (opcional)
     * @param page Número de página (base 0)
     * @param pageSize Tamaño de página
     * @return Respuesta con listings y metadatos de paginación
     */
    public ListingListResponse execute(
            String brand, String size, String condition,
            String gender, String color,
            Double minPrice, Double maxPrice,
            int page, int pageSize) {
        
        List<Listing> listings = listingRepository.findPublishedListings(
                brand, size, condition, gender, color,
                minPrice, maxPrice, page, pageSize
        );
        
        long total = listingRepository.countPublishedListings(
                brand, size, condition, gender, color,
                minPrice, maxPrice
        );
        
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
