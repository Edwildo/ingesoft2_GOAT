package com.goat.cart.entrypoints.rest;

import com.goat.cart.application.dto.AddItemRequest;
import com.goat.cart.application.dto.CartItemResponse;
import com.goat.cart.application.dto.CartResponse;
import com.goat.cart.application.usecases.AddItemToCartUseCase;
import com.goat.cart.application.usecases.ClearCartUseCase;
import com.goat.cart.application.usecases.GetCartUseCase;
import com.goat.cart.application.usecases.RemoveItemFromCartUseCase;
import com.goat.cart.domain.exceptions.CannotAddOwnListingException;
import com.goat.cart.domain.exceptions.CartNotFoundException;
import com.goat.cart.domain.exceptions.ItemAlreadyInCartException;
import com.goat.cart.domain.exceptions.ItemNotFoundException;
import com.goat.cart.domain.exceptions.ListingNotAvailableException;
import com.goat.identity.infrastructure.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller para operaciones del carrito.
 * Requiere JWT autenticación. Sigue el mismo patrón que ListingController.
 * 
 * Arquitectura: Entrypoint layer en hexagonal architecture.
 * Usa SecurityContextHelper para extraer userId del JWT automáticamente.
 */
@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final AddItemToCartUseCase addItemToCartUseCase;
    private final GetCartUseCase getCartUseCase;
    private final RemoveItemFromCartUseCase removeItemFromCartUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final SecurityContextHelper securityContextHelper;

    public CartController(
            AddItemToCartUseCase addItemToCartUseCase,
            GetCartUseCase getCartUseCase,
            RemoveItemFromCartUseCase removeItemFromCartUseCase,
            ClearCartUseCase clearCartUseCase,
            SecurityContextHelper securityContextHelper
    ) {
        this.addItemToCartUseCase = addItemToCartUseCase;
        this.getCartUseCase = getCartUseCase;
        this.removeItemFromCartUseCase = removeItemFromCartUseCase;
        this.clearCartUseCase = clearCartUseCase;
        this.securityContextHelper = securityContextHelper;
    }

    /**
     * Agrega un item al carrito.
     * POST /api/cart/items
     * Requiere autenticación JWT.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @Valid @RequestBody AddItemRequest request
    ) {
        UUID userId = securityContextHelper.getCurrentUserId();

        addItemToCartUseCase.execute(userId, request.listingId());

        // Obtener el carrito actualizado
        var cart = getCartUseCase.execute(userId);
        var response = mapToCartResponse(cart);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene el carrito del usuario autenticado.
     * GET /api/cart
     * Requiere autenticación JWT.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        UUID userId = securityContextHelper.getCurrentUserId();

        var cart = getCartUseCase.execute(userId);
        var response = mapToCartResponse(cart);

        return ResponseEntity.ok(response);
    }

    /**
     * Remueve un item del carrito.
     * DELETE /api/cart/items/{itemId}
     * Requiere autenticación JWT.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> removeItem(
            @PathVariable UUID itemId
    ) {
        UUID userId = securityContextHelper.getCurrentUserId();

        removeItemFromCartUseCase.execute(userId, itemId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Item removido del carrito");

        return ResponseEntity.ok(response);
    }

    /**
     * Vacía el carrito del usuario.
     * DELETE /api/cart
     * Requiere autenticación JWT.
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart() {
        UUID userId = securityContextHelper.getCurrentUserId();

        clearCartUseCase.execute(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Carrito vaciado");

        return ResponseEntity.ok(response);
    }

    /**
     * Mapea Cart (dominio) a CartResponse (DTO).
     */
    private CartResponse mapToCartResponse(com.goat.cart.domain.entities.Cart cart) {
        var itemResponses = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getListingId(),
                        item.getSneakerSku(),
                        item.getSize(),
                        item.getPrice(),
                        item.getBrand(),
                        item.getColor(),
                        item.getCondition(),
                        item.getCoverImage(),
                        item.getAddedAt()
                ))
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getStatus().name(),
                itemResponses,
                cart.getTotalItems(),
                cart.calculateTotal(),
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    /**
     * Manejador de excepciones específicas del carrito.
     */
    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCartNotFound(CartNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Cart not found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleItemNotFound(ItemNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Item not found");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ListingNotAvailableException.class)
    public ResponseEntity<Map<String, String>> handleListingNotAvailable(ListingNotAvailableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Listing not available");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(CannotAddOwnListingException.class)
    public ResponseEntity<Map<String, String>> handleCannotAddOwnListing(CannotAddOwnListingException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Cannot add own listing");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(ItemAlreadyInCartException.class)
    public ResponseEntity<Map<String, String>> handleItemAlreadyInCart(ItemAlreadyInCartException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Item already in cart");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Bad request");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
