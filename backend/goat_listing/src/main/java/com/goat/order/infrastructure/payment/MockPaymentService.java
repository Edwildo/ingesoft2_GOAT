package com.goat.order.infrastructure.payment;

import com.goat.order.domain.exceptions.PaymentFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Servicio mock para simular pagos.
 * 
 * Simula un delay de 2-3 segundos y retorna éxito en el 90% de los casos.
 */
@Service
public class MockPaymentService {
    private static final Logger logger = LoggerFactory.getLogger(MockPaymentService.class);
    private static final Random random = new Random();
    private static final double SUCCESS_RATE = 0.9; // 90% éxito

    /**
     * Procesa un pago mock.
     * 
     * @param amount Monto del pago
     * @return ID del pago mock
     * @throws PaymentFailedException si el pago falla (10% de probabilidad)
     */
    public String processPayment(java.math.BigDecimal amount) throws PaymentFailedException {
        logger.info("Procesando pago mock por monto: {}", amount);
        
        // Simular delay de 2-3 segundos
        try {
            int delaySeconds = 2 + random.nextInt(2); // 2 o 3 segundos
            TimeUnit.SECONDS.sleep(delaySeconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupción durante el delay del pago mock");
        }

        // 90% éxito, 10% fallo
        if (random.nextDouble() < SUCCESS_RATE) {
            String paymentId = "MOCK_PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            logger.info("Pago mock exitoso. Payment ID: {}", paymentId);
            return paymentId;
        } else {
            logger.warn("Pago mock fallido (simulación)");
            throw new PaymentFailedException("El pago fue rechazado. Por favor, intenta nuevamente.");
        }
    }
}

