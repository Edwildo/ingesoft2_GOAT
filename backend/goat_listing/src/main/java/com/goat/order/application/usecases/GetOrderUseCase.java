package com.goat.order.application.usecases;

import com.goat.order.domain.entities.Order;
import com.goat.order.domain.exceptions.OrderNotFoundException;
import com.goat.order.ports.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso para obtener órdenes.
 */
@Component
public class GetOrderUseCase {
    private final OrderRepository orderRepository;

    public GetOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Obtiene una orden por su ID, validando que pertenezca al usuario.
     */
    public Order execute(UUID orderId, UUID userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.getBuyerId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }

        return order;
    }

    /**
     * Obtiene todas las órdenes de un usuario.
     */
    public List<Order> findByBuyerId(UUID buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }
}

