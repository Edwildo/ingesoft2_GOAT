package com.goat.order.domain.entities;

import com.goat.order.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un registro en el historial de estados de una orden.
 * Permite trazabilidad completa de los cambios de estado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {
    private UUID id;
    private UUID orderId;
    private OrderStatus status;
    private LocalDateTime changedAt;
    private UUID changedBy;
    private String notes;

    /**
     * Factory method para crear un registro de historial.
     */
    public static OrderStatusHistory create(UUID orderId, OrderStatus status, UUID changedBy, String notes) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.id = UUID.randomUUID();
        history.orderId = orderId;
        history.status = status;
        history.changedAt = LocalDateTime.now();
        history.changedBy = changedBy;
        history.notes = notes;
        return history;
    }
}
