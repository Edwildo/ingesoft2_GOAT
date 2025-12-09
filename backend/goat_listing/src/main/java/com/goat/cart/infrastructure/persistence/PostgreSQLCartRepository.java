package com.goat.cart.infrastructure.persistence;

import com.goat.cart.domain.entities.Cart;
import com.goat.cart.domain.enums.CartStatus;
import com.goat.cart.infrastructure.persistence.mapper.CartMapper;
import com.goat.cart.infrastructure.persistence.repository.CartJpaRepository;
import com.goat.cart.ports.CartRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del puerto CartRepository usando PostgreSQL (JPA).
 */
@Component
public class PostgreSQLCartRepository implements CartRepository {

    private final CartJpaRepository cartJpaRepository;
    private final CartMapper cartMapper;

    public PostgreSQLCartRepository(CartJpaRepository cartJpaRepository, CartMapper cartMapper) {
        this.cartJpaRepository = cartJpaRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    public Optional<Cart> findActiveByUser(UUID userId) {
        return cartJpaRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .map(cartMapper::toDomain);
    }

    @Override
    public void save(Cart cart) {
        var entity = cartMapper.toEntity(cart);
        cartJpaRepository.save(entity);
    }

    @Override
    public void removeItem(UUID cartId, UUID itemId) {
        var cartOpt = cartJpaRepository.findById(cartId);
        if (cartOpt.isPresent()) {
            var cart = cartOpt.get();
            cart.getItems().removeIf(item -> item.getId().equals(itemId));
            cart.setUpdatedAt(java.time.LocalDateTime.now());
            cartJpaRepository.save(cart);
        }
    }

    @Override
    public void clearCart(UUID cartId) {
        var cartOpt = cartJpaRepository.findById(cartId);
        if (cartOpt.isPresent()) {
            var cart = cartOpt.get();
            cart.getItems().clear();
            cart.setUpdatedAt(java.time.LocalDateTime.now());
            cartJpaRepository.save(cart);
        }
    }
}
