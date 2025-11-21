package com.goat.identity.adapters.external;

import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.ports.OtpVerificationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador que implementa OtpVerificationPort usando REST para comunicarse
 * con el servicio Python que gestiona OTPs.
 */
@Component
public class RestOtpVerificationAdapter implements OtpVerificationPort {
    private final RestTemplate restTemplate;
    private final String pythonServiceBaseUrl;

    public RestOtpVerificationAdapter(
            RestTemplate restTemplate,
            @Value("${services.python.base-url:http://localhost:8082}") String pythonServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceBaseUrl = pythonServiceBaseUrl;
    }

    @Override
    public boolean isEmailConfirmed(Email email) {
        try {
            String url = pythonServiceBaseUrl + "/api/auth/verify-email-status?email=" + email.getValue();
            ResponseEntity<EmailVerificationResponse> response = restTemplate.getForEntity(
                    url,
                    EmailVerificationResponse.class
            );

            return response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().isConfirmed());
        } catch (RestClientException e) {
            return false;
        }
    }

    /**
     * Clase interna para mapear la respuesta del servicio Python.
     * Spring usa automáticamente los setters para deserializar JSON.
     */
    private static class EmailVerificationResponse {
        private Boolean confirmed;

        public Boolean isConfirmed() {
            return confirmed;
        }

        // Este setter es necesario para que Spring pueda deserializar el JSON
        // de la respuesta del servicio Python, aunque no se llame explícitamente
        public void setConfirmed(Boolean confirmed) {
            this.confirmed = confirmed;
        }
    }
}

