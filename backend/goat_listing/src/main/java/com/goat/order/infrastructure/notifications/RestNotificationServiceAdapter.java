package com.goat.order.infrastructure.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador que envía notificaciones usando REST para comunicarse
 * con el servicio Python que gestiona las notificaciones por email.
 */
@Component
public class RestNotificationServiceAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RestNotificationServiceAdapter.class);
    
    private final RestTemplate restTemplate;
    private final String pythonServiceBaseUrl;

    public RestNotificationServiceAdapter(
            RestTemplate restTemplate,
            @Value("${services.python.base-url:${PYTHON_SERVICE_URL:http://localhost:8082}}") String pythonServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceBaseUrl = pythonServiceBaseUrl;
        logger.info("RestNotificationServiceAdapter inicializado con URL base: {}", this.pythonServiceBaseUrl);
    }

    /**
     * Envía un email de confirmación de orden.
     * 
     * @param email Email del destinatario
     * @param orderId ID de la orden
     * @param orderTotal Total de la orden
     * @param orderItemsCount Cantidad de items en la orden
     */
    public void sendOrderConfirmationEmail(String email, String orderId, BigDecimal orderTotal, int orderItemsCount) {
        try {
            String url = pythonServiceBaseUrl + "/api/notifications/order-confirmation";
            logger.info("Enviando email de confirmación de orden - URL: {}, Email: {}, OrderId: {}", url, email, orderId);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("order_id", orderId);
            requestBody.put("order_total", orderTotal.doubleValue());
            requestBody.put("order_items_count", orderItemsCount);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            logger.debug("Request body: {}", requestBody);

            ResponseEntity<NotificationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    NotificationResponse.class
            );

            logger.info("Respuesta del servicio de notificaciones - Status: {}, Body: {}", 
                    response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                NotificationResponse body = response.getBody();
                if (Boolean.TRUE.equals(body.getSuccess())) {
                    logger.info("Email de confirmación de orden enviado exitosamente a {}", email);
                } else {
                    logger.warn("El servicio de notificaciones reportó fallo: {}", body.getMessage());
                }
            } else {
                logger.warn("Respuesta inesperada al enviar email de confirmación: {}", response.getStatusCode());
            }
        } catch (RestClientException e) {
            // No fallar la creación de orden si el email falla
            logger.error("Error REST al enviar email de confirmación de orden a {}: {}", email, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error inesperado al enviar email de confirmación de orden", e);
        }
    }

    /**
     * Clase interna para mapear la respuesta del servicio Python.
     */
    private static class NotificationResponse {
        private Boolean success;
        private String message;

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

