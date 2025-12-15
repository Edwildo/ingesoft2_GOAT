package com.goat.listing.ports;

import com.goat.listing.domain.entities.Listing;
import com.goat.listing.domain.enums.ListingStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (interfaz) para el repositorio de listings.
 * Define las operaciones de persistencia sin depender de implementación.
 */
public interface ListingRepository {
    /**
     * Busca un listing por su ID.
     *
     * @param id ID del listing
     * @return Listing encontrado o Optional vacío
     */
    Optional<Listing> findById(UUID id);

    /**
     * Busca listings públicos (PUBLISHED) con filtros opcionales.
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
     * @return Lista de listings públicos
     */
    List<Listing> findPublishedListings(String brand, String size, String condition,
                                        String gender, String color, 
                                        Double minPrice, Double maxPrice,
                                        int page, int pageSize);

    /**
     * Cuenta el total de listings públicos que cumplen los filtros.
     *
     * @param brand Filtro por marca (opcional)
     * @param size Filtro por talla (opcional)
     * @param condition Filtro por condición (opcional)
     * @param gender Filtro por género (opcional)
     * @param color Filtro por color (opcional)
     * @param minPrice Precio mínimo (opcional)
     * @param maxPrice Precio máximo (opcional)
     * @return Total de listings
     */
    long countPublishedListings(String brand, String size, String condition,
                                String gender, String color,
                                Double minPrice, Double maxPrice);

    /**
     * Busca listings de un seller específico con filtro opcional por estado.
     *
     * @param sellerId ID del seller
     * @param status Filtro por estado (opcional)
     * @param page Número de página (base 0)
     * @param pageSize Tamaño de página
     * @return Lista de listings del seller
     */
    List<Listing> findBySellerId(UUID sellerId, ListingStatus status, int page, int pageSize);

    /**
     * Cuenta el total de listings de un seller que cumplen el filtro de estado.
     *
     * @param sellerId ID del seller
     * @param status Filtro por estado (opcional)
     * @return Total de listings
     */
    long countBySellerId(UUID sellerId, ListingStatus status);

    /**
     * Guarda un listing (crear o actualizar).
     *
     * @param listing Listing a guardar
     * @return Listing guardado
     */
    Listing save(Listing listing);

    /**
     * Verifica si un listing existe y pertenece a un seller.
     *
     * @param listingId ID del listing
     * @param sellerId ID del seller
     * @return true si el listing existe y pertenece al seller
     */
    boolean existsByIdAndSellerId(UUID listingId, UUID sellerId);

    /**
     * Elimina un listing por su ID.
     *
     * @param id ID del listing a eliminar
     */
    void deleteById(UUID id);
}
