package com.goat.order.adapters.persistence.repository;

import com.goat.order.adapters.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para OrderEntity.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    /**
     * Busca todas las órdenes de un comprador ordenadas por fecha de creación descendente.
     */
    List<OrderEntity> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);
}

