package com.goat.cart.infrastructure.persistence.repository;

import com.goat.cart.infrastructure.persistence.entity.CartEntity;
import com.goat.cart.domain.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio Spring Data para CartEntity.
 */
@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, UUID> {

    /**
     * Encuentra el carrito ACTIVE de un usuario.
     */
    Optional<CartEntity> findByUserIdAndStatus(UUID userId, CartStatus status);
}
