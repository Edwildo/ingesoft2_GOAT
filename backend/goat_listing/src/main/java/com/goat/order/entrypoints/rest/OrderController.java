package com.goat.order.entrypoints.rest;

import com.goat.order.application.dto.CreateOrderRequest;
import com.goat.order.application.dto.OrderResponse;
import com.goat.order.application.dto.OrderStatusHistoryResponse;
import com.goat.order.application.usecases.CreateOrderUseCase;
import com.goat.order.application.usecases.GetOrderStatusHistoryUseCase;
import com.goat.order.application.usecases.GetOrderUseCase;
import com.goat.order.domain.exceptions.EmptyCartException;
import com.goat.order.domain.exceptions.OrderNotFoundException;
import com.goat.order.domain.exceptions.PaymentFailedException;
import com.goat.identity.infrastructure.security.SecurityContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de órdenes.
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final GetOrderStatusHistoryUseCase getOrderStatusHistoryUseCase;
    private final SecurityContextHelper securityContextHelper;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            GetOrderUseCase getOrderUseCase,
            GetOrderStatusHistoryUseCase getOrderStatusHistoryUseCase,
            SecurityContextHelper securityContextHelper) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.getOrderStatusHistoryUseCase = getOrderStatusHistoryUseCase;
        this.securityContextHelper = securityContextHelper;
    }

    /**
     * Crea una nueva orden desde el carrito.
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            UUID userId = securityContextHelper.getCurrentUserId();
            logger.info("=== CREATE ORDER REQUEST ===");
            logger.info("Usuario ID: {}", userId);
            logger.info("Método de envío: {}", request.getShippingMethod());
            if (request.getShippingAddress() != null) {
                CreateOrderRequest.ShippingAddressDto address = request.getShippingAddress();
                logger.info("Dirección de envío:");
                logger.info("  - Calle: {}", address.getStreet());
                logger.info("  - Ciudad: {}", address.getCity());
                logger.info("  - Estado/Provincia: {}", address.getState());
                logger.info("  - Código Postal: {}", address.getPostalCode());
                logger.info("  - País: {}", address.getCountry());
            } else {
                logger.warn("Shipping address es null en la request");
            }
            logger.info("============================");

            var order = createOrderUseCase.execute(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(OrderResponse.fromDomain(order));

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación al crear orden: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));

        } catch (EmptyCartException e) {
            logger.warn("Intento de crear orden con carrito vacío");
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));

        } catch (PaymentFailedException e) {
            logger.warn("Pago fallido al crear orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(new ErrorResponse(e.getMessage()));

        } catch (Exception e) {
            logger.error("Error inesperado al crear orden", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al procesar la orden"));
        }
    }

    /**
     * Obtiene todas las órdenes del usuario.
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<?> getMyOrders() {
        try {
            UUID userId = securityContextHelper.getCurrentUserId();
            logger.info("Obteniendo órdenes del usuario {}", userId);

            var orders = getOrderUseCase.findByBuyerId(userId);
            List<OrderResponse> responses = orders.stream()
                    .map(OrderResponse::fromDomain)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            logger.error("Error al obtener órdenes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener las órdenes"));
        }
    }

    /**
     * Obtiene una orden específica por su ID.
     * GET /api/orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable UUID id) {
        try {
            UUID userId = securityContextHelper.getCurrentUserId();
            logger.info("Obteniendo orden {} para usuario {}", id, userId);

            var order = getOrderUseCase.execute(id, userId);
            return ResponseEntity.ok(OrderResponse.fromDomain(order));

        } catch (OrderNotFoundException e) {
            logger.warn("Orden no encontrada: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error al obtener orden {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener la orden"));
        }
    }

    /**
     * Obtiene el historial de estados de una orden.
     * GET /api/orders/{id}/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<?> getOrderHistory(@PathVariable UUID id) {
        try {
            UUID userId = securityContextHelper.getCurrentUserId();
            logger.info("Obteniendo historial de orden {} para usuario {}", id, userId);

            var history = getOrderStatusHistoryUseCase.execute(id, userId);
            List<OrderStatusHistoryResponse> responses = OrderStatusHistoryResponse.fromDomainList(history);

            return ResponseEntity.ok(responses);

        } catch (OrderNotFoundException e) {
            logger.warn("Orden no encontrada: {}", id);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            logger.error("Error al obtener historial de orden {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener el historial"));
        }
    }

    /**
     * DTO para respuestas de error.
     */
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

