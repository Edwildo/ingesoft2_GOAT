package com.goat.identity.adapters.external;

import com.goat.identity.domain.valueobjects.Email;
import com.goat.identity.ports.OtpGenerationPort;
import com.goat.identity.ports.OtpValidationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador que implementa OtpGenerationPort y OtpValidationPort usando REST
 * para comunicarse con el servicio Python que gestiona OTPs.
 */
@Component
public class RestOtpServiceAdapter implements OtpGenerationPort, OtpValidationPort {
    private final RestTemplate restTemplate;
    private final String pythonServiceBaseUrl;

    public RestOtpServiceAdapter(
            RestTemplate restTemplate,
            @Value("${services.python.base-url:http://localhost:8082}") String pythonServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceBaseUrl = pythonServiceBaseUrl;
    }

    @Override
    public boolean generateOtp(Email email, String purpose) {
        try {
            String url = pythonServiceBaseUrl + "/api/auth/otp";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email.getValue());
            requestBody.put("purpose", purpose);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<OtpGenerationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    OtpGenerationResponse.class
            );

            return response.getStatusCode() == HttpStatus.CREATED
                    && response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().isSuccess());
        } catch (RestClientException e) {
            return false;
        }
    }

    @Override
    public boolean validateOtp(Email email, String otp, String purpose) {
        try {
            String url = pythonServiceBaseUrl + "/api/auth/verify";
            
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email.getValue());
            requestBody.put("otp", otp);
            requestBody.put("purpose", purpose);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<OtpValidationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    OtpValidationResponse.class
            );

            return response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null
                    && Boolean.TRUE.equals(response.getBody().isValid());
        } catch (RestClientException e) {
            return false;
        }
    }

    /**
     * Clase interna para mapear la respuesta de generación de OTP del servicio Python.
     */
    private static class OtpGenerationResponse {
        private Boolean success;
        private String message;
        private Integer expiresInMinutes;

        public Boolean isSuccess() {
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

        public Integer getExpiresInMinutes() {
            return expiresInMinutes;
        }

        public void setExpiresInMinutes(Integer expiresInMinutes) {
            this.expiresInMinutes = expiresInMinutes;
        }
    }

    /**
     * Clase interna para mapear la respuesta de validación de OTP del servicio Python.
     */
    private static class OtpValidationResponse {
        private Boolean success;
        private String message;
        private Boolean valid;

        public Boolean isSuccess() {
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

        public Boolean isValid() {
            return valid;
        }

        public void setValid(Boolean valid) {
            this.valid = valid;
        }
    }
}

