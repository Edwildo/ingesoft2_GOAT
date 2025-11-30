package com.goat.listing.adapters.persistence;

import com.goat.listing.adapters.persistence.entity.ListingEntity;
import com.goat.listing.adapters.persistence.mapper.ListingMapper;
import com.goat.listing.adapters.persistence.repository.ListingJpaRepository;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.domain.enums.ListingStatus;
import com.goat.listing.ports.ListingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que implementa ListingRepository usando PostgreSQL y Spring Data JPA.
 */
@Component
public class PostgreSQLListingRepository implements ListingRepository {
    private final ListingJpaRepository jpaRepository;

    public PostgreSQLListingRepository(ListingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        Optional<ListingEntity> entity = jpaRepository.findById(id);
        return entity.map(ListingMapper::toDomain);
    }

    @Override
    public List<Listing> findPublishedListings(
            String brand, String size, String condition,
            String gender, String color,
            Double minPrice, Double maxPrice,
            int page, int pageSize) {
        
        Pageable pageable = PageRequest.of(page, pageSize);
        
        BigDecimal minPriceDecimal = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceDecimal = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        
        List<ListingEntity> entities = jpaRepository.findPublishedListingsWithFilters(
                brand, size, condition, gender, color,
                minPriceDecimal, maxPriceDecimal,
                pageable
        );
        
        return ListingMapper.toDomainList(entities);
    }

    @Override
    public long countPublishedListings(
            String brand, String size, String condition,
            String gender, String color,
            Double minPrice, Double maxPrice) {
        
        BigDecimal minPriceDecimal = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceDecimal = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        
        return jpaRepository.countPublishedListingsWithFilters(
                brand, size, condition, gender, color,
                minPriceDecimal, maxPriceDecimal
        );
    }

    @Override
    public List<Listing> findBySellerId(UUID sellerId, ListingStatus status, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        
        List<ListingEntity> entities;
        if (status != null) {
            entities = jpaRepository.findBySellerIdAndStatus(sellerId, status, pageable).getContent();
        } else {
            entities = jpaRepository.findBySellerId(sellerId, pageable).getContent();
        }
        
        return ListingMapper.toDomainList(entities);
    }

    @Override
    public long countBySellerId(UUID sellerId, ListingStatus status) {
        if (status != null) {
            return jpaRepository.findBySellerIdAndStatus(sellerId, status, Pageable.unpaged()).getTotalElements();
        } else {
            return jpaRepository.findBySellerId(sellerId, Pageable.unpaged()).getTotalElements();
        }
    }

    @Override
    public Listing save(Listing listing) {
        ListingEntity entity = ListingMapper.toEntity(listing);
        ListingEntity savedEntity = jpaRepository.save(entity);
        return ListingMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByIdAndSellerId(UUID listingId, UUID sellerId) {
        return jpaRepository.existsByIdAndSellerId(listingId, sellerId);
    }
}
