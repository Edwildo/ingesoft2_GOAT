package com.goat.cart.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un item dentro del carrito.
 * No es un agregado raíz, es una entidad hija de Cart.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private UUID id;
    private UUID listingId;
    private String sneakerSku;
    private String size;
    private BigDecimal price;        // Snapshot del precio al agregar
    private String brand;
    private String color;
    private String condition;
    private String coverImage;
    private LocalDateTime addedAt;

    /**
     * Constructor para crear un nuevo item (cuando se agrega al carrito).
     */
    public CartItem(UUID listingId, String sneakerSku, String size, BigDecimal price,
                    String brand, String color, String condition, String coverImage) {
        this.id = UUID.randomUUID();
        this.listingId = listingId;
        this.sneakerSku = sneakerSku;
        this.size = size;
        this.price = price;
        this.brand = brand;
        this.color = color;
        this.condition = condition;
        this.coverImage = coverImage;
        this.addedAt = LocalDateTime.now();
    }
}
