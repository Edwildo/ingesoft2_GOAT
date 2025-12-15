package com.goat.order.domain.entities;

import com.goat.order.domain.enums.OrderStatus;
import com.goat.order.domain.enums.ShippingMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Agregado raíz del bounded context Order.
 * Representa una orden de compra.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private UUID id;
    private UUID buyerId;
    private UUID cartId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private ShippingAddress shippingAddress;
    private ShippingMethod shippingMethod;
    private String paymentId;
    private List<OrderItem> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method para crear una nueva orden.
     */
    public static Order createNew(
            UUID buyerId,
            UUID cartId,
            BigDecimal totalAmount,
            ShippingAddress shippingAddress,
            ShippingMethod shippingMethod,
            List<OrderItem> items) {
        Order order = new Order();
        order.id = UUID.randomUUID();
        order.buyerId = buyerId;
        order.cartId = cartId;
        order.status = OrderStatus.PENDING;
        order.totalAmount = totalAmount;
        order.shippingAddress = shippingAddress;
        order.shippingMethod = shippingMethod;
        order.items = new ArrayList<>(items);
        order.createdAt = LocalDateTime.now();
        order.updatedAt = LocalDateTime.now();
        return order;
    }

    /**
     * Inicia el proceso de pago.
     */
    public void startPayment() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo órdenes PENDING pueden iniciar pago");
        }
        this.status = OrderStatus.PAYMENT_PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Confirma la orden después de un pago exitoso.
     */
    public void confirm(String paymentId) {
        if (status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Solo órdenes PAYMENT_PENDING pueden confirmarse");
        }
        this.status = OrderStatus.CONFIRMED;
        this.paymentId = paymentId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca la orden como cancelada.
     */
    public void cancel() {
        if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("No se puede cancelar una orden entregada");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca la orden como en envío.
     */
    public void markAsShipping() {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Solo órdenes CONFIRMED pueden marcarse como en envío");
        }
        this.status = OrderStatus.SHIPPING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca la orden como entregada.
     */
    public void markAsDelivered() {
        if (status != OrderStatus.SHIPPING) {
            throw new IllegalStateException("Solo órdenes SHIPPING pueden marcarse como entregadas");
        }
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica si la orden puede ser cancelada.
     */
    public boolean canBeCancelled() {
        return status != OrderStatus.DELIVERED && status != OrderStatus.CANCELLED;
    }
}

