package com.goat.order.application.usecases;

import com.goat.cart.adapters.external.RestCartServiceAdapter;
import com.goat.cart.domain.entities.Cart;
import com.goat.identity.infrastructure.security.SecurityContextHelper;
import com.goat.order.application.dto.CreateOrderRequest;
import com.goat.order.domain.entities.Order;
import com.goat.order.domain.entities.OrderItem;
import com.goat.order.domain.entities.OrderStatusHistory;
import com.goat.order.domain.entities.ShippingAddress;
import com.goat.order.domain.enums.OrderStatus;
import com.goat.order.domain.exceptions.EmptyCartException;
import com.goat.order.domain.exceptions.PaymentFailedException;
import com.goat.order.infrastructure.notifications.RestNotificationServiceAdapter;
import com.goat.order.infrastructure.payment.MockPaymentService;
import com.goat.order.ports.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para crear una orden desde el carrito.
 * 
 * Flujo:
 * 1. Obtiene el carrito del usuario desde Python
 * 2. Valida que el carrito no esté vacío
 * 3. Crea la orden en estado PENDING
 * 4. Registra en el historial
 * 5. Procesa el pago mock
 * 6. Si el pago es exitoso, confirma la orden y vacía el carrito
 * 7. Si el pago falla, mantiene la orden en PENDING
 */
@Component
public class CreateOrderUseCase {
    private static final Logger logger = LoggerFactory.getLogger(CreateOrderUseCase.class);

    private final OrderRepository orderRepository;
    private final RestCartServiceAdapter cartServiceAdapter;
    private final MockPaymentService paymentService;
    private final RestNotificationServiceAdapter notificationServiceAdapter;
    private final SecurityContextHelper securityContextHelper;

    public CreateOrderUseCase(
            OrderRepository orderRepository,
            RestCartServiceAdapter cartServiceAdapter,
            MockPaymentService paymentService,
            RestNotificationServiceAdapter notificationServiceAdapter,
            SecurityContextHelper securityContextHelper) {
        this.orderRepository = orderRepository;
        this.cartServiceAdapter = cartServiceAdapter;
        this.paymentService = paymentService;
        this.notificationServiceAdapter = notificationServiceAdapter;
        this.securityContextHelper = securityContextHelper;
    }

    @Transactional
    public Order execute(UUID userId, CreateOrderRequest request) {
        logger.info("=== CREATE ORDER USE CASE ===");
        logger.info("Usuario ID: {}", userId);

        // 1. Validar dirección de envío
        if (request.getShippingAddress() == null) {
            logger.error("Shipping address es null en CreateOrderRequest");
            throw new IllegalArgumentException("La dirección de envío es requerida");
        }

        ShippingAddress shippingAddress = request.getShippingAddress().toDomain();
        logger.info("Dirección convertida a dominio:");
        logger.info("  - Street: {}", shippingAddress.getStreet());
        logger.info("  - City: {}", shippingAddress.getCity());
        logger.info("  - State: {}", shippingAddress.getState());
        logger.info("  - PostalCode: {}", shippingAddress.getPostalCode());
        logger.info("  - Country: {}", shippingAddress.getCountry());
        logger.info("  - isValid(): {}", shippingAddress.isValid());

        if (!shippingAddress.isValid()) {
            logger.error("La dirección de envío NO es válida - validación falló");
            throw new IllegalArgumentException("La dirección de envío no es válida");
        }
        logger.info("Dirección de envío validada correctamente");

        // 2. Obtener carrito desde Python
        Cart cart = cartServiceAdapter.findActiveByUser(userId)
                .orElseThrow(() -> new EmptyCartException("El carrito está vacío"));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("El carrito está vacío");
        }

        // 3. Calcular total
        BigDecimal totalAmount = cart.calculateTotal();

        // 4. Crear items de la orden (snapshot del carrito)
        logger.info("Cart items count: {}", cart.getItems().size());
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    logger.info("Processing cart item - ListingId: {}, SneakerSku: {}, Size: {}, Brand: {}, Condition: {}, Price: {}",
                            cartItem.getListingId(), cartItem.getSneakerSku(), cartItem.getSize(),
                            cartItem.getBrand(), cartItem.getCondition(), cartItem.getPrice());
                    
                    // Validar que los campos requeridos estén presentes en el cartItem
                    if (cartItem.getSneakerSku() == null || cartItem.getSneakerSku().trim().isEmpty()) {
                        throw new IllegalArgumentException(
                                String.format("El cartItem para listing %s no tiene sneakerSku", cartItem.getListingId()));
                    }
                    
                    if (cartItem.getCondition() == null || cartItem.getCondition().trim().isEmpty()) {
                        throw new IllegalArgumentException(
                                String.format("El cartItem para listing %s no tiene condition", cartItem.getListingId()));
                    }
                    
                    return OrderItem.fromCartItem(
                            null, // orderId se asignará después
                            cartItem.getListingId(),
                            cartItem.getPrice(),
                            cartItem.getSneakerSku(),
                            cartItem.getSize(),
                            cartItem.getBrand(),
                            cartItem.getColor(),
                            cartItem.getCondition(),
                            cartItem.getCoverImage()
                    );
                })
                .collect(Collectors.toList());
        
        logger.info("Created {} order items", orderItems.size());

        // 5. Crear orden en estado PENDING asociada al carrito
        Order order = Order.createNew(
                userId,
                cart.getId(), // Asociar el carrito a la orden
                totalAmount,
                shippingAddress,
                request.getShippingMethod(),
                orderItems
        );

        // 6. Guardar orden
        order = orderRepository.save(order);

        // 7. Registrar estado inicial en historial
        OrderStatusHistory initialHistory = OrderStatusHistory.create(
                order.getId(),
                OrderStatus.PENDING,
                userId,
                "Orden creada desde carrito " + cart.getId()
        );
        orderRepository.saveStatusHistory(initialHistory);

        // 8. Procesar pago mock
        try {
            order.startPayment();
            orderRepository.save(order);

            // Registrar cambio a PAYMENT_PENDING
            OrderStatusHistory paymentPendingHistory = OrderStatusHistory.create(
                    order.getId(),
                    OrderStatus.PAYMENT_PENDING,
                    userId,
                    "Iniciando procesamiento de pago"
            );
            orderRepository.saveStatusHistory(paymentPendingHistory);

            String paymentId = paymentService.processPayment(order.getTotalAmount());
            
            // 9. Confirmar orden
            order.confirm(paymentId);
            orderRepository.save(order);

            // Registrar confirmación en historial
            OrderStatusHistory confirmedHistory = OrderStatusHistory.create(
                    order.getId(),
                    OrderStatus.CONFIRMED,
                    userId,
                    "Pago confirmado. Payment ID: " + paymentId + ". Carrito " + cart.getId() + " procesado."
            );
            orderRepository.saveStatusHistory(confirmedHistory);

            // 10. Vaciar carrito en Python después de confirmar la orden
            // La orden ya está guardada con la referencia al cart_id para trazabilidad
            cartServiceAdapter.clearCart(userId);

            // 11. Enviar email de confirmación (no bloquea si falla)
            // Capturar valores finales para usar en la lambda
            final UUID orderId = order.getId();
            final BigDecimal orderTotal = order.getTotalAmount();
            final int orderItemsCount = order.getItems().size();
            
            securityContextHelper.getCurrentUserEmail().ifPresent(email -> {
                try {
                    notificationServiceAdapter.sendOrderConfirmationEmail(
                            email,
                            orderId.toString(),
                            orderTotal,
                            orderItemsCount
                    );
                    logger.info("Email de confirmación enviado a {}", email);
                } catch (Exception e) {
                    logger.warn("Error al enviar email de confirmación (no crítico): {}", e.getMessage());
                }
            });

            logger.info("Orden {} creada y confirmada exitosamente para usuario {}", order.getId(), userId);
            return order;

        } catch (PaymentFailedException e) {
            logger.warn("Pago fallido para orden {}: {}", order.getId(), e.getMessage());
            
            // Registrar fallo en historial
            OrderStatusHistory failedHistory = OrderStatusHistory.create(
                    order.getId(),
                    OrderStatus.PENDING,
                    userId,
                    "Pago fallido: " + e.getMessage()
            );
            orderRepository.saveStatusHistory(failedHistory);

            // La orden queda en PENDING, el usuario puede reintentar
            throw e;
        }
    }
}

