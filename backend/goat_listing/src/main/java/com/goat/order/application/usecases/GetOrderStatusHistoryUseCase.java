package com.goat.order.application.usecases;

import com.goat.order.domain.entities.Order;
import com.goat.order.domain.entities.OrderStatusHistory;
import com.goat.order.domain.exceptions.OrderNotFoundException;
import com.goat.order.ports.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para obtener el historial de estados de una orden.
 */
@Component
public class GetOrderStatusHistoryUseCase {
    private final OrderRepository orderRepository;

    public GetOrderStatusHistoryUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Obtiene el historial de estados de una orden, validando que pertenezca al usuario.
     */
    public List<OrderStatusHistory> execute(UUID orderId, UUID userId) {
        // Validar que la orden existe y pertenece al usuario
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getBuyerId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }

        return orderRepository.findStatusHistoryByOrderId(orderId);
    }
}

