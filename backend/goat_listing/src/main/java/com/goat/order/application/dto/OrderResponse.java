package com.goat.order.application.dto;

import com.goat.order.domain.entities.Order;
import com.goat.order.domain.entities.OrderItem;
import com.goat.order.domain.entities.ShippingAddress;
import com.goat.order.domain.enums.OrderStatus;
import com.goat.order.domain.enums.ShippingMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO de respuesta para una orden.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private UUID id;
    private UUID buyerId;
    private UUID cartId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private ShippingAddressDto shippingAddress;
    private ShippingMethod shippingMethod;
    private String paymentId;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse fromDomain(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setBuyerId(order.getBuyerId());
        response.setCartId(order.getCartId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingMethod(order.getShippingMethod());
        response.setPaymentId(order.getPaymentId());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Mapear ShippingAddress
        ShippingAddress address = order.getShippingAddress();
        response.setShippingAddress(new ShippingAddressDto(
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        ));

        // Mapear items
        response.setItems(order.getItems().stream()
                .map(OrderItemDto::fromDomain)
                .collect(Collectors.toList()));

        return response;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddressDto {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private UUID id;
        private UUID listingId;
        private BigDecimal price;
        private String sneakerSku;
        private String size;
        private String brand;
        private String color;
        private String condition;
        private String coverImage;

        public static OrderItemDto fromDomain(OrderItem item) {
            return new OrderItemDto(
                    item.getId(),
                    item.getListingId(),
                    item.getPrice(),
                    item.getSneakerSku(),
                    item.getSize(),
                    item.getBrand(),
                    item.getColor(),
                    item.getCondition(),
                    item.getCoverImage()
            );
        }
    }
}

