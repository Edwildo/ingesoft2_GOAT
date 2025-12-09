package com.goat.cart.domain.entities;

import com.goat.cart.domain.enums.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agregado raíz del bounded context Cart.
 * Representa el carrito de compras de un usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private UUID id;
    private UUID userId;
    private CartStatus status;
    private List<CartItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method para crear un nuevo carrito.
     */
    public static Cart createNew(UUID userId) {
        Cart cart = new Cart();
        cart.id = UUID.randomUUID();
        cart.userId = userId;
        cart.status = CartStatus.ACTIVE;
        cart.items = new ArrayList<>();
        cart.createdAt = LocalDateTime.now();
        cart.updatedAt = LocalDateTime.now();
        return cart;
    }

    /**
     * Agrega un item al carrito si no existe ya.
     * @throws com.goat.cart.domain.exceptions.ItemAlreadyInCartException si el item ya existe
     */
    public void addItem(CartItem item) {
        boolean exists = items.stream()
                .anyMatch(i -> i.getListingId().equals(item.getListingId()));
        if (exists) {
            throw new com.goat.cart.domain.exceptions.ItemAlreadyInCartException(
                    "El item con listing " + item.getListingId() + " ya está en el carrito"
            );
        }
        items.add(item);
        updatedAt = LocalDateTime.now();
    }

    /**
     * Remueve un item del carrito por su ID.
     */
    public void removeItem(UUID itemId) {
        items.removeIf(i -> i.getId().equals(itemId));
        updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si un item existe en el carrito.
     */
    public boolean containsItem(UUID listingId) {
        return items.stream()
                .anyMatch(i -> i.getListingId().equals(listingId));
    }

    /**
     * Calcula el precio total del carrito.
     */
    public BigDecimal calculateTotal() {
        return items.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna la cantidad de items en el carrito.
     */
    public int getTotalItems() {
        return items.size();
    }

    /**
     * Vacía el carrito.
     */
    public void clear() {
        items.clear();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si el carrito está vacío.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
