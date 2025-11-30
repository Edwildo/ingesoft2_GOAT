package com.goat.listing.adapters.persistence.repository;

import com.goat.listing.adapters.persistence.entity.ListingEntity;
import com.goat.listing.domain.enums.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para ListingEntity.
 */
@Repository
public interface ListingJpaRepository extends JpaRepository<ListingEntity, UUID> {
    
    /**
     * Busca listings por seller con filtro opcional por estado.
     */
    Page<ListingEntity> findBySellerIdAndStatus(UUID sellerId, ListingStatus status, Pageable pageable);
    
    /**
     * Busca listings por seller sin filtro de estado.
     */
    Page<ListingEntity> findBySellerId(UUID sellerId, Pageable pageable);
    
    /**
     * Verifica si un listing existe y pertenece a un seller.
     */
    boolean existsByIdAndSellerId(UUID listingId, UUID sellerId);
    
    /**
     * Busca listings públicos (PUBLISHED) con filtros opcionales usando query nativa.
     * Los filtros se aplican solo si los parámetros no son null.
     */
    @Query(value = "SELECT * FROM listing.listings l " +
           "WHERE l.status = 'PUBLISHED' " +
           "AND (:brand IS NULL OR LOWER(l.brand) = LOWER(:brand)) " +
           "AND (:size IS NULL OR l.size = :size) " +
           "AND (:condition IS NULL OR LOWER(l.condition) = LOWER(:condition)) " +
           "AND (:gender IS NULL OR LOWER(l.gender) = LOWER(:gender)) " +
           "AND (:color IS NULL OR LOWER(l.color) = LOWER(:color)) " +
           "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR l.price <= :maxPrice) " +
           "ORDER BY l.created_at DESC", nativeQuery = true)
    List<ListingEntity> findPublishedListingsWithFilters(
            @Param("brand") String brand,
            @Param("size") String size,
            @Param("condition") String condition,
            @Param("gender") String gender,
            @Param("color") String color,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
    
    /**
     * Cuenta listings públicos (PUBLISHED) con filtros opcionales.
     */
    @Query(value = "SELECT COUNT(*) FROM listing.listings l " +
           "WHERE l.status = 'PUBLISHED' " +
           "AND (:brand IS NULL OR LOWER(l.brand) = LOWER(:brand)) " +
           "AND (:size IS NULL OR l.size = :size) " +
           "AND (:condition IS NULL OR LOWER(l.condition) = LOWER(:condition)) " +
           "AND (:gender IS NULL OR LOWER(l.gender) = LOWER(:gender)) " +
           "AND (:color IS NULL OR LOWER(l.color) = LOWER(:color)) " +
           "AND (:minPrice IS NULL OR l.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR l.price <= :maxPrice)", nativeQuery = true)
    long countPublishedListingsWithFilters(
            @Param("brand") String brand,
            @Param("size") String size,
            @Param("condition") String condition,
            @Param("gender") String gender,
            @Param("color") String color,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
