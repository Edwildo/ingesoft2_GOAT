package com.goat.order.adapters.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad JPA que mapea a la tabla order.order_items.
 */
@Entity
@Table(name = "order_items", schema = "order")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "UUID")
    private UUID orderId;

    @Column(name = "listing_id", nullable = false, columnDefinition = "UUID")
    private UUID listingId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "sneaker_sku", nullable = false, length = 80)
    private String sneakerSku;

    @Column(nullable = false, length = 16)
    private String size;

    @Column(nullable = false, length = 60)
    private String brand;

    @Column(nullable = false, length = 40)
    private String color;

    @Column(nullable = false, length = 24)
    private String condition;

    @Column(name = "cover_image", length = 256)
    private String coverImage;
}
