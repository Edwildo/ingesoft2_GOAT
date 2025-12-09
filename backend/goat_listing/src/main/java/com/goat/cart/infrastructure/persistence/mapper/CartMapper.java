package com.goat.cart.infrastructure.persistence.mapper;

import com.goat.cart.domain.entities.Cart;
import com.goat.cart.domain.entities.CartItem;
import com.goat.cart.infrastructure.persistence.entity.CartEntity;
import com.goat.cart.infrastructure.persistence.entity.CartItemEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Mapper para convertir entre entidades JPA y entidades de dominio.
 */
@Component
public class CartMapper {

    /**
     * Convierte una CartEntity (JPA) a Cart (dominio).
     */
    public Cart toDomain(CartEntity entity) {
        if (entity == null) {
            return null;
        }

        Cart cart = new Cart();
        cart.setId(entity.getId());
        cart.setUserId(entity.getUserId());
        cart.setStatus(entity.getStatus());
        cart.setCreatedAt(entity.getCreatedAt());
        cart.setUpdatedAt(entity.getUpdatedAt());

        cart.setItems(entity.getItems().stream()
                .map(this::toDomainItem)
                .toList());

        return cart;
    }

    /**
     * Convierte un CartItemEntity (JPA) a CartItem (dominio).
     */
    public CartItem toDomainItem(CartItemEntity entity) {
        if (entity == null) {
            return null;
        }

        CartItem item = new CartItem();
        item.setId(entity.getId());
        item.setListingId(entity.getListingId());
        item.setSneakerSku(entity.getSneakerSku());
        item.setSize(entity.getSize());
        item.setPrice(entity.getPrice());
        item.setBrand(entity.getBrand());
        item.setColor(entity.getColor());
        item.setCondition(entity.getCondition());
        item.setCoverImage(entity.getCoverImage());
        item.setAddedAt(entity.getAddedAt());

        return item;
    }

    /**
     * Convierte un Cart (dominio) a CartEntity (JPA).
     */
    public CartEntity toEntity(Cart domain) {
        if (domain == null) {
            return null;
        }

        CartEntity entity = new CartEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        var items = domain.getItems().stream()
                .map(item -> toEntityItem(item, entity))
                .toList();
        entity.setItems(new ArrayList<>(items));

        return entity;
    }

    /**
     * Convierte un CartItem (dominio) a CartItemEntity (JPA).
     */
    public CartItemEntity toEntityItem(CartItem domain, CartEntity cartEntity) {
        if (domain == null) {
            return null;
        }

        CartItemEntity entity = new CartItemEntity();
        entity.setId(domain.getId());
        entity.setCart(cartEntity);
        entity.setListingId(domain.getListingId());
        entity.setSneakerSku(domain.getSneakerSku());
        entity.setSize(domain.getSize());
        entity.setPrice(domain.getPrice());
        entity.setBrand(domain.getBrand());
        entity.setColor(domain.getColor());
        entity.setCondition(domain.getCondition());
        entity.setCoverImage(domain.getCoverImage());
        entity.setAddedAt(domain.getAddedAt());

        return entity;
    }
}
