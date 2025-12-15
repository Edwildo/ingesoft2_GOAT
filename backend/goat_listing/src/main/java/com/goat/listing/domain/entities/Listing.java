package com.goat.listing.domain.entities;

import com.goat.listing.domain.enums.ListingStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio que representa un listing de un seller.
 * Agregado raíz del contexto Listing.
 */
@Getter
@Setter
@NoArgsConstructor
public class Listing {
    private UUID id;
    private UUID sellerId;
    private String sneakerSku;
    private String size;
    private String condition;
    private String gender;
    private String brand;
    private String color;
    private BigDecimal price;
    private ListingStatus status;
    private String coverImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Listing(UUID sellerId, String sneakerSku, String size, String condition,
                   String gender, String brand, String color, BigDecimal price, String coverImage) {
        this.sellerId = sellerId;
        this.sneakerSku = sneakerSku;
        this.size = size;
        this.condition = condition;
        this.gender = gender;
        this.brand = brand;
        this.color = color;
        this.price = price;
        this.coverImage = coverImage;
        this.status = ListingStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si el listing puede ser editado.
     * Regla de negocio: listings DRAFT y ARCHIVED pueden editarse.
     */
    public boolean canBeEdited() {
        return this.status == ListingStatus.DRAFT || this.status == ListingStatus.ARCHIVED;
    }

    /**
     * Verifica si el listing puede ser publicado.
     * Regla de negocio: listings DRAFT y ARCHIVED pueden publicarse.
     */
    public boolean canBePublished() {
        return this.status == ListingStatus.DRAFT || this.status == ListingStatus.ARCHIVED;
    }

    /**
     * Verifica si el listing puede ser archivado.
     * Regla de negocio: solo listings PUBLISHED pueden archivarse.
     */
    public boolean canBeArchived() {
        return this.status == ListingStatus.PUBLISHED;
    }

    /**
     * Publica el listing (DRAFT -> PUBLISHED o ARCHIVED -> PUBLISHED).
     */
    public void publish() {
        if (!canBePublished()) {
            throw new IllegalStateException("Solo listings en estado DRAFT o ARCHIVED pueden publicarse");
        }
        this.status = ListingStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Archiva el listing (PUBLISHED -> ARCHIVED).
     */
    public void archive() {
        if (!canBeArchived()) {
            throw new IllegalStateException("Solo listings en estado PUBLISHED pueden archivarse");
        }
        this.status = ListingStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Sobrescribe equals para comparar solo por ID (identidad de entidad).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Listing listing = (Listing) o;
        return id != null && id.equals(listing.id);
    }

    /**
     * Sobrescribe hashCode para usar solo el ID.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
