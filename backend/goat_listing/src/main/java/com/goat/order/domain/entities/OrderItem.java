package com.goat.order.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item de una orden. Representa un snapshot del listing al momento de la compra.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private UUID id;
    private UUID orderId;
    private UUID listingId;
    private BigDecimal price;
    private String sneakerSku;
    private String size;
    private String brand;
    private String color;
    private String condition;
    private String coverImage;

    /**
     * Factory method para crear un OrderItem desde un CartItem.
     */
    public static OrderItem fromCartItem(UUID orderId, UUID listingId, BigDecimal price,
                                        String sneakerSku, String size, String brand,
                                        String color, String condition, String coverImage) {
        OrderItem item = new OrderItem();
        item.id = UUID.randomUUID();
        item.orderId = orderId;
        item.listingId = listingId;
        item.price = price;
        item.sneakerSku = sneakerSku;
        item.size = size;
        item.brand = brand;
        item.color = color;
        item.condition = condition;
        item.coverImage = coverImage;
        return item;
    }
}
