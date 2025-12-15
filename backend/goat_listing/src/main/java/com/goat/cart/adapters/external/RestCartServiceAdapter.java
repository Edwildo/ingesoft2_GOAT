package com.goat.cart.adapters.external;

import com.goat.cart.adapters.external.dto.AddItemRequestPython;
import com.goat.cart.adapters.external.dto.CartItemResponsePython;
import com.goat.cart.adapters.external.dto.CartResponsePython;
import com.goat.cart.domain.entities.Cart;
import com.goat.cart.domain.entities.CartItem;
import com.goat.cart.domain.enums.CartStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que implementa la comunicación HTTP con el servicio Python
 * que gestiona el carrito en MongoDB.
 * 
 * La URL base del servicio Python se configura mediante:
 * - Variable de entorno: PYTHON_SERVICE_URL
 * - application.yml: services.python.base-url
 * - Valor por defecto: http://localhost:8082
 */
@Component
public class RestCartServiceAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RestCartServiceAdapter.class);
    
    private final RestTemplate restTemplate;
    private final String pythonServiceBaseUrl;

    public RestCartServiceAdapter(
            RestTemplate restTemplate,
            @Value("${services.python.base-url:${PYTHON_SERVICE_URL:http://localhost:8082}}") String pythonServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceBaseUrl = pythonServiceBaseUrl;
        logger.info("RestCartServiceAdapter inicializado con URL base: {}", this.pythonServiceBaseUrl);
    }

    /**
     * Obtiene el carrito activo de un usuario desde el servicio Python.
     */
    public Optional<Cart> findActiveByUser(UUID userId) {
        try {
            String url = pythonServiceBaseUrl + "/api/cart";
            logger.debug("Obteniendo carrito del usuario {} desde Python: {}", userId, url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId.toString());
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<CartResponsePython> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    CartResponsePython.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Cart cart = mapToDomain(response.getBody(), userId);
                logger.debug("Carrito obtenido exitosamente para usuario {}", userId);
                return Optional.of(cart);
            }
            
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            // 404 es esperado cuando el carrito no existe
            logger.debug("Carrito no encontrado para usuario {} (404)", userId);
            return Optional.empty();
        } catch (RestClientException e) {
            logger.error("Error al obtener carrito del usuario {} desde Python: {}", userId, e.getMessage());
            throw new RuntimeException("Error al comunicarse con el servicio de carrito", e);
        }
    }

    /**
     * Agrega un item al carrito en el servicio Python.
     */
    public void addItem(UUID userId, AddItemRequestPython request) {
        try {
            String url = pythonServiceBaseUrl + "/api/cart/items";
            logger.debug("Agregando item al carrito del usuario {} en Python: {}", userId, url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId.toString());
            headers.set("Content-Type", "application/json");
            HttpEntity<AddItemRequestPython> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                logger.debug("Item agregado exitosamente al carrito del usuario {}", userId);
            } else {
                logger.warn("Respuesta inesperada al agregar item: {}", response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al agregar item al carrito: {} - {}", e.getStatusCode(), e.getMessage());
            throw mapHttpException(e);
        } catch (RestClientException e) {
            logger.error("Error al agregar item al carrito del usuario {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error al comunicarse con el servicio de carrito", e);
        }
    }

    /**
     * Elimina un item del carrito en el servicio Python.
     */
    public void removeItem(UUID userId, UUID itemId) {
        try {
            String url = pythonServiceBaseUrl + "/api/cart/items/" + itemId;
            logger.debug("Eliminando item {} del carrito del usuario {} en Python: {}", itemId, userId, url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId.toString());
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );

            logger.debug("Item {} eliminado exitosamente del carrito del usuario {}", itemId, userId);
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al eliminar item del carrito: {} - {}", e.getStatusCode(), e.getMessage());
            throw mapHttpException(e);
        } catch (RestClientException e) {
            logger.error("Error al eliminar item {} del carrito del usuario {}: {}", itemId, userId, e.getMessage());
            throw new RuntimeException("Error al comunicarse con el servicio de carrito", e);
        }
    }

    /**
     * Vacía el carrito en el servicio Python.
     */
    public void clearCart(UUID userId) {
        try {
            String url = pythonServiceBaseUrl + "/api/cart";
            logger.debug("Vaciando carrito del usuario {} en Python: {}", userId, url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId.toString());
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );

            logger.debug("Carrito del usuario {} vaciado exitosamente", userId);
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al vaciar carrito: {} - {}", e.getStatusCode(), e.getMessage());
            throw mapHttpException(e);
        } catch (RestClientException e) {
            logger.error("Error al vaciar carrito del usuario {}: {}", userId, e.getMessage());
            throw new RuntimeException("Error al comunicarse con el servicio de carrito", e);
        }
    }

    /**
     * Mapea CartResponsePython a Cart (dominio).
     */
    private Cart mapToDomain(CartResponsePython pythonResponse, UUID userId) {
        Cart cart = new Cart();
        cart.setId(UUID.fromString(pythonResponse.id()));
        cart.setUserId(userId);
        cart.setStatus(CartStatus.ACTIVE);
        
        List<CartItem> items = pythonResponse.items().stream()
                .map(this::mapItemToDomain)
                .toList();
        cart.setItems(items);
        cart.setUpdatedAt(pythonResponse.updatedAt());
        cart.setCreatedAt(pythonResponse.updatedAt()); // Python no envía createdAt, usamos updatedAt
        
        return cart;
    }

    /**
     * Mapea CartItemResponsePython a CartItem (dominio).
     */
    private CartItem mapItemToDomain(CartItemResponsePython pythonItem) {
        CartItem item = new CartItem();
        item.setId(UUID.fromString(pythonItem.id()));
        item.setListingId(UUID.fromString(pythonItem.listingId()));
        item.setSneakerSku(pythonItem.sneakerSku());
        item.setSize(pythonItem.size());
        item.setPrice(pythonItem.price());
        item.setBrand(pythonItem.brand());
        item.setColor(pythonItem.color());
        item.setCondition(pythonItem.condition());
        item.setCoverImage(pythonItem.coverImage());
        item.setAddedAt(pythonItem.createdAt());
        return item;
    }

    /**
     * Mapea excepciones HTTP a excepciones de dominio.
     */
    private RuntimeException mapHttpException(HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            return new com.goat.cart.domain.exceptions.CartNotFoundException("Carrito no encontrado");
        } else if (e.getStatusCode() == HttpStatus.CONFLICT) {
            return new com.goat.cart.domain.exceptions.ItemAlreadyInCartException("El item ya está en el carrito");
        } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            return new com.goat.cart.domain.exceptions.ListingNotAvailableException("El listing no está disponible");
        } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
            return new com.goat.cart.domain.exceptions.CannotAddOwnListingException("No puedes agregar tus propios listings");
        }
        return new RuntimeException("Error en el servicio de carrito: " + e.getMessage());
    }
}

