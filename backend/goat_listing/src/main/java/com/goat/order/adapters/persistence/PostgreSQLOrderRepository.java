package com.goat.order.adapters.persistence;

import com.goat.order.adapters.persistence.entity.OrderEntity;
import com.goat.order.adapters.persistence.entity.OrderItemEntity;
import com.goat.order.adapters.persistence.entity.OrderStatusHistoryEntity;
import com.goat.order.adapters.persistence.mapper.OrderMapper;
import com.goat.order.adapters.persistence.repository.OrderItemJpaRepository;
import com.goat.order.adapters.persistence.repository.OrderJpaRepository;
import com.goat.order.adapters.persistence.repository.OrderStatusHistoryJpaRepository;
import com.goat.order.domain.entities.Order;
import com.goat.order.domain.entities.OrderStatusHistory;
import com.goat.order.ports.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador que implementa OrderRepository usando PostgreSQL y Spring Data JPA.
 */
@Component
public class PostgreSQLOrderRepository implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository;

    public PostgreSQLOrderRepository(
            OrderJpaRepository orderJpaRepository,
            OrderItemJpaRepository orderItemJpaRepository,
            OrderStatusHistoryJpaRepository orderStatusHistoryJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
        this.orderItemJpaRepository = orderItemJpaRepository;
        this.orderStatusHistoryJpaRepository = orderStatusHistoryJpaRepository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        // Guardar la orden
        OrderEntity orderEntity = OrderMapper.toEntity(order);
        OrderEntity savedOrderEntity = orderJpaRepository.save(orderEntity);

        // Guardar los items
        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> {
                    OrderItemEntity entity = OrderMapper.itemToEntity(item);
                    entity.setOrderId(savedOrderEntity.getId());
                    return entity;
                })
                .collect(Collectors.toList());
        orderItemJpaRepository.saveAll(itemEntities);

        // Obtener los items guardados para retornar la orden completa
        List<OrderItemEntity> savedItemEntities = orderItemJpaRepository.findByOrderId(savedOrderEntity.getId());
        return OrderMapper.toDomain(savedOrderEntity, savedItemEntities);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        Optional<OrderEntity> orderEntity = orderJpaRepository.findById(orderId);
        if (orderEntity.isEmpty()) {
            return Optional.empty();
        }

        List<OrderItemEntity> itemEntities = orderItemJpaRepository.findByOrderId(orderId);
        return Optional.of(OrderMapper.toDomain(orderEntity.get(), itemEntities));
    }

    @Override
    public List<Order> findByBuyerId(UUID buyerId) {
        List<OrderEntity> orderEntities = orderJpaRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
        return orderEntities.stream()
                .map(entity -> {
                    List<OrderItemEntity> itemEntities = orderItemJpaRepository.findByOrderId(entity.getId());
                    return OrderMapper.toDomain(entity, itemEntities);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID orderId) {
        return orderJpaRepository.existsById(orderId);
    }

    /**
     * Guarda un registro en el historial de estados.
     */
    @Transactional
    public void saveStatusHistory(OrderStatusHistory history) {
        OrderStatusHistoryEntity entity = OrderMapper.historyToEntity(history);
        orderStatusHistoryJpaRepository.save(entity);
    }

    /**
     * Obtiene el historial de estados de una orden.
     */
    public List<OrderStatusHistory> findStatusHistoryByOrderId(UUID orderId) {
        List<OrderStatusHistoryEntity> entities = orderStatusHistoryJpaRepository.findByOrderIdOrderByChangedAtDesc(orderId);
        return entities.stream()
                .map(OrderMapper::historyToDomain)
                .collect(Collectors.toList());
    }
}

