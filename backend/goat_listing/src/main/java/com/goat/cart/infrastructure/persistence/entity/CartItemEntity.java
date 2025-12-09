package com.goat.cart.infrastructure.persistence.entity;

import com.goat.listing.adapters.persistence.entity.ListingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que mapea a la tabla cart.cart_items.
 */
@Entity
@Table(name = "cart_items", schema = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;

    @Column(name = "listing_id", nullable = false, columnDefinition = "UUID")
    private UUID listingId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "sneaker_sku", nullable = false, length = 80)
    private String sneakerSku;

    @Column(name = "size", nullable = false, length = 16)
    private String size;

    @Column(name = "brand", nullable = false, length = 60)
    private String brand;

    @Column(name = "color", nullable = false, length = 40)
    private String color;

    @Column(name = "condition", nullable = false, length = 24)
    private String condition;

    @Column(name = "cover_image", length = 256)
    private String coverImage;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;
}
