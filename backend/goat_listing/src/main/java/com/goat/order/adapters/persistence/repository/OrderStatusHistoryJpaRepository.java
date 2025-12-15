package com.goat.order.adapters.persistence.repository;

import com.goat.order.adapters.persistence.entity.OrderStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para OrderStatusHistoryEntity.
 */
@Repository
public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderStatusHistoryEntity, UUID> {
    /**
     * Busca el historial de estados de una orden ordenado por fecha descendente.
     */
    List<OrderStatusHistoryEntity> findByOrderIdOrderByChangedAtDesc(UUID orderId);
}

