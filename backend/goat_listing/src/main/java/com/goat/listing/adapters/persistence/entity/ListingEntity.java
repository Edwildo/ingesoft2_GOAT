package com.goat.listing.adapters.persistence.entity;

import com.goat.listing.domain.enums.ListingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que mapea a la tabla listing.listings.
 * Separada del dominio (Listing) para mantener la Arquitectura Hexagonal.
 */
@Entity
@Table(name = "listings", schema = "listing")
@Getter
@Setter
@NoArgsConstructor
public class ListingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "seller_id", nullable = false, columnDefinition = "UUID")
    private UUID sellerId;

    @Column(name = "sneaker_sku", nullable = false, length = 100)
    private String sneakerSku;

    @Column(length = 10)
    private String size;

    @Column(length = 50)
    private String condition;

    @Column(length = 20)
    private String gender;

    @Column(length = 100)
    private String brand;

    @Column(length = 50)
    private String color;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ListingStatus status;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
