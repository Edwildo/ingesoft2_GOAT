package com.goat.order.adapters.persistence.entity;

import com.goat.order.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA que mapea a la tabla order.order_status_history.
 */
@Entity
@Table(name = "order_status_history", schema = "order")
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "UUID")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by", columnDefinition = "UUID")
    private UUID changedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

