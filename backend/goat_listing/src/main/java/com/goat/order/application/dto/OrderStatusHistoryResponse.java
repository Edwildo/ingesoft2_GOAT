package com.goat.order.application.dto;

import com.goat.order.domain.entities.OrderStatusHistory;
import com.goat.order.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO de respuesta para el historial de estados de una orden.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistoryResponse {
    private UUID id;
    private UUID orderId;
    private OrderStatus status;
    private LocalDateTime changedAt;
    private UUID changedBy;
    private String notes;

    public static OrderStatusHistoryResponse fromDomain(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getId(),
                history.getOrderId(),
                history.getStatus(),
                history.getChangedAt(),
                history.getChangedBy(),
                history.getNotes()
        );
    }

    public static List<OrderStatusHistoryResponse> fromDomainList(List<OrderStatusHistory> histories) {
        return histories.stream()
                .map(OrderStatusHistoryResponse::fromDomain)
                .collect(Collectors.toList());
    }
}

