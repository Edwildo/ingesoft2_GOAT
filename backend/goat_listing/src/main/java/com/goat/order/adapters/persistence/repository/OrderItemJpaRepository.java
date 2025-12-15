package com.goat.order.adapters.persistence.repository;

import com.goat.order.adapters.persistence.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para OrderItemEntity.
 */
@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, UUID> {
    /**
     * Busca todos los items de una orden.
     */
    List<OrderItemEntity> findByOrderId(UUID orderId);

    /**
     * Cuenta cuántos order_items están asociados a un listing.
     * Usa query nativa para asegurar que funcione correctamente.
     * Nota: "order" es una palabra reservada en PostgreSQL, por lo que debe ir entre comillas dobles.
     */
    @Query(value = "SELECT COUNT(*) FROM \"order\".order_items WHERE listing_id = :listingId", nativeQuery = true)
    long countByListingId(@Param("listingId") UUID listingId);
}

