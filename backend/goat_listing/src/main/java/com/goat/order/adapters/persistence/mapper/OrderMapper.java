package com.goat.order.adapters.persistence.mapper;

import com.goat.order.adapters.persistence.entity.OrderEntity;
import com.goat.order.adapters.persistence.entity.OrderItemEntity;
import com.goat.order.adapters.persistence.entity.OrderStatusHistoryEntity;
import com.goat.order.adapters.persistence.entity.ShippingAddressEntity;
import com.goat.order.domain.entities.Order;
import com.goat.order.domain.entities.OrderItem;
import com.goat.order.domain.entities.OrderStatusHistory;
import com.goat.order.domain.entities.ShippingAddress;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades de dominio y entidades JPA de Order.
 */
public class OrderMapper {

    /**
     * Convierte una entidad JPA a una entidad de dominio.
     */
    public static Order toDomain(OrderEntity entity, List<OrderItemEntity> itemEntities) {
        if (entity == null) {
            return null;
        }

        Order order = new Order();
        order.setId(entity.getId());
        order.setBuyerId(entity.getBuyerId());
        order.setCartId(entity.getCartId());
        order.setStatus(entity.getStatus());
        order.setTotalAmount(entity.getTotalAmount());
        order.setShippingMethod(entity.getShippingMethod());
        order.setPaymentId(entity.getPaymentId());
        order.setCreatedAt(entity.getCreatedAt());
        order.setUpdatedAt(entity.getUpdatedAt());

        // Convertir ShippingAddressEntity a ShippingAddress del dominio
        ShippingAddress shippingAddress = toDomain(entity.getShippingAddress());
        order.setShippingAddress(shippingAddress);

        // Mapear items
        List<OrderItem> items = itemEntities.stream()
                .map(OrderMapper::itemToDomain)
                .collect(Collectors.toList());
        order.setItems(items);

        return order;
    }

    /**
     * Convierte una entidad de dominio a una entidad JPA.
     */
    public static OrderEntity toEntity(Order order) {
        if (order == null) {
            return null;
        }

        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId());
        entity.setBuyerId(order.getBuyerId());
        entity.setCartId(order.getCartId());
        entity.setStatus(order.getStatus());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setShippingMethod(order.getShippingMethod());
        entity.setPaymentId(order.getPaymentId());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        // Convertir ShippingAddress del dominio a ShippingAddressEntity
        ShippingAddressEntity shippingAddressEntity = toEntity(order.getShippingAddress());
        entity.setShippingAddress(shippingAddressEntity);

        return entity;
    }

    /**
     * Convierte OrderItemEntity a OrderItem.
     */
    public static OrderItem itemToDomain(OrderItemEntity entity) {
        if (entity == null) {
            return null;
        }

        OrderItem item = new OrderItem();
        item.setId(entity.getId());
        item.setOrderId(entity.getOrderId());
        item.setListingId(entity.getListingId());
        item.setPrice(entity.getPrice());
        item.setSneakerSku(entity.getSneakerSku());
        item.setSize(entity.getSize());
        item.setBrand(entity.getBrand());
        item.setColor(entity.getColor());
        item.setCondition(entity.getCondition());
        item.setCoverImage(entity.getCoverImage());
        return item;
    }

    /**
     * Convierte OrderItem a OrderItemEntity.
     */
    public static OrderItemEntity itemToEntity(OrderItem item) {
        if (item == null) {
            return null;
        }

        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(item.getId());
        entity.setOrderId(item.getOrderId());
        entity.setListingId(item.getListingId());
        entity.setPrice(item.getPrice());
        entity.setSneakerSku(item.getSneakerSku());
        entity.setSize(item.getSize());
        entity.setBrand(item.getBrand());
        entity.setColor(item.getColor());
        entity.setCondition(item.getCondition());
        entity.setCoverImage(item.getCoverImage());
        return entity;
    }

    /**
     * Convierte OrderStatusHistoryEntity a OrderStatusHistory.
     */
    public static OrderStatusHistory historyToDomain(OrderStatusHistoryEntity entity) {
        if (entity == null) {
            return null;
        }

        OrderStatusHistory history = new OrderStatusHistory();
        history.setId(entity.getId());
        history.setOrderId(entity.getOrderId());
        history.setStatus(entity.getStatus());
        history.setChangedAt(entity.getChangedAt());
        history.setChangedBy(entity.getChangedBy());
        history.setNotes(entity.getNotes());
        return history;
    }

    /**
     * Convierte OrderStatusHistory a OrderStatusHistoryEntity.
     */
    public static OrderStatusHistoryEntity historyToEntity(OrderStatusHistory history) {
        if (history == null) {
            return null;
        }

        OrderStatusHistoryEntity entity = new OrderStatusHistoryEntity();
        entity.setId(history.getId());
        entity.setOrderId(history.getOrderId());
        entity.setStatus(history.getStatus());
        entity.setChangedAt(history.getChangedAt());
        entity.setChangedBy(history.getChangedBy());
        entity.setNotes(history.getNotes());
        return entity;
    }

    /**
     * Convierte ShippingAddressEntity a ShippingAddress del dominio.
     */
    public static ShippingAddress toDomain(ShippingAddressEntity entity) {
        if (entity == null) {
            return null;
        }
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setStreet(entity.getStreet());
        shippingAddress.setCity(entity.getCity());
        shippingAddress.setState(entity.getState());
        shippingAddress.setPostalCode(entity.getPostalCode());
        shippingAddress.setCountry(entity.getCountry());
        return shippingAddress;
    }

    /**
     * Convierte ShippingAddress del dominio a ShippingAddressEntity.
     */
    public static ShippingAddressEntity toEntity(ShippingAddress shippingAddress) {
        if (shippingAddress == null) {
            return null;
        }
        ShippingAddressEntity entity = new ShippingAddressEntity();
        entity.setStreet(shippingAddress.getStreet());
        entity.setCity(shippingAddress.getCity());
        entity.setState(shippingAddress.getState());
        entity.setPostalCode(shippingAddress.getPostalCode());
        entity.setCountry(shippingAddress.getCountry());
        return entity;
    }
}

