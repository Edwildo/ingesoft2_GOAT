package com.goat.order.ports;

import com.goat.order.domain.entities.Order;
import com.goat.order.domain.entities.OrderStatusHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (interfaz) del repositorio de órdenes.
 * Define las operaciones de persistencia para el agregado Order.
 */
public interface OrderRepository {
    /**
     * Guarda una orden (crea o actualiza).
     */
    Order save(Order order);

    /**
     * Busca una orden por su ID.
     */
    Optional<Order> findById(UUID orderId);

    /**
     * Busca todas las órdenes de un comprador.
     */
    List<Order> findByBuyerId(UUID buyerId);

    /**
     * Verifica si existe una orden con el ID dado.
     */
    boolean existsById(UUID orderId);

    /**
     * Guarda un registro en el historial de estados.
     */
    void saveStatusHistory(OrderStatusHistory history);

    /**
     * Obtiene el historial de estados de una orden.
     */
    List<OrderStatusHistory> findStatusHistoryByOrderId(UUID orderId);
}

