package com.goat.listing.adapters.external;

import com.goat.listing.application.dto.CreateSneakerRequest;
import com.goat.listing.application.dto.SearchSneakersResponse;
import com.goat.listing.application.dto.SneakerResponse;
import com.goat.listing.ports.CatalogServicePort;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador que implementa CatalogServicePort usando REST para comunicarse
 * con el servicio Python que gestiona el catálogo de sneakers.
 * 
 * La URL base del servicio Python se configura mediante:
 * - Variable de entorno: PYTHON_SERVICE_URL
 * - application.yml: services.python.base-url
 * - Valor por defecto: http://localhost:8082
 */
@Component
public class RestCatalogServiceAdapter implements CatalogServicePort {
    private static final Logger logger = LoggerFactory.getLogger(RestCatalogServiceAdapter.class);
    
    private final RestTemplate restTemplate;
    private final String pythonServiceBaseUrl;

    public RestCatalogServiceAdapter(
            RestTemplate restTemplate,
            @Value("${services.python.base-url:${PYTHON_SERVICE_URL:http://localhost:8082}}") String pythonServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceBaseUrl = pythonServiceBaseUrl;
        logger.info("RestCatalogServiceAdapter inicializado con URL base: {}", this.pythonServiceBaseUrl);
    }

    @Override
    public boolean validateSneakerSku(String sku) {
        try {
            String url = pythonServiceBaseUrl + "/api/catalog/sneakers/" + sku;
            logger.debug("Validando SKU con servicio Python: {}", url);
            
            ResponseEntity<SneakerPythonResponse> response = restTemplate.getForEntity(
                    url,
                    SneakerPythonResponse.class
            );

            // Si la respuesta es 200 OK y tiene body, el SKU existe
            boolean exists = response.getStatusCode() == HttpStatus.OK
                    && response.getBody() != null
                    && response.getBody().getSku() != null;
            
            if (exists) {
                logger.debug("SKU {} validado exitosamente en catálogo Python", sku);
            } else {
                logger.warn("SKU {} no encontrado en catálogo Python", sku);
            }
            
            return exists;
        } catch (HttpClientErrorException.NotFound e) {
            // 404 es esperado cuando el SKU no existe - no es un error crítico
            logger.debug("SKU {} no encontrado en catálogo Python (404): {}", sku, e.getMessage());
            return false;
        } catch (HttpClientErrorException e) {
            // Otros errores HTTP (400, 500, etc.) son problemas reales
            logger.error("Error HTTP al validar SKU {} con servicio Python en {}: {} - {}", 
                    sku, pythonServiceBaseUrl, e.getStatusCode(), e.getMessage());
            return false;
        } catch (RestClientException e) {
            // Errores de conexión, timeout, etc. son problemas críticos
            logger.error("Error de conexión al validar SKU {} con servicio Python en {}: {}", 
                    sku, pythonServiceBaseUrl, e.getMessage());
            logger.debug("Stack trace completo:", e);
            return false;
        }
    }

    @Override
    public Optional<SneakerResponse> getSneakerBySku(String sku) {
        try {
            String url = pythonServiceBaseUrl + "/api/catalog/sneakers/" + sku;
            logger.debug("Obteniendo sneaker por SKU: {}", url);

            ResponseEntity<SneakerPythonResponse> response = restTemplate.getForEntity(
                    url,
                    SneakerPythonResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                SneakerResponse sneakerResponse = convertToSneakerResponse(response.getBody());
                logger.debug("Sneaker {} obtenido exitosamente", sku);
                return Optional.of(sneakerResponse);
            }

            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            logger.debug("SKU {} no encontrado en catálogo Python (404)", sku);
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al obtener SKU {} con servicio Python en {}: {} - {}",
                    sku, pythonServiceBaseUrl, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Error al obtener sneaker del catálogo: " + e.getMessage(), e);
        } catch (RestClientException e) {
            logger.error("Error de conexión al obtener SKU {} con servicio Python en {}: {}",
                    sku, pythonServiceBaseUrl, e.getMessage());
            throw new RuntimeException("Error de conexión con el servicio de catálogo", e);
        }
    }

    @Override
    public SneakerResponse createSneaker(CreateSneakerRequest request) {
        try {
            String url = pythonServiceBaseUrl + "/api/catalog/sneakers";
            logger.debug("Creando sneaker en servicio Python: {}", url);

            SneakerPythonRequest pythonRequest = convertToPythonRequest(request);
            HttpEntity<SneakerPythonRequest> httpEntity = new HttpEntity<>(pythonRequest);

            ResponseEntity<SneakerPythonResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    httpEntity,
                    SneakerPythonResponse.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                SneakerResponse sneakerResponse = convertToSneakerResponse(response.getBody());
                logger.info("Sneaker {} creado exitosamente en catálogo Python", request.getSku());
                return sneakerResponse;
            }

            throw new RuntimeException("Error al crear sneaker: respuesta inesperada del servicio");
        } catch (HttpClientErrorException.Conflict e) {
            logger.warn("SKU {} ya existe en el catálogo (409)", request.getSku());
            throw new IllegalArgumentException("Ya existe un sneaker con SKU '" + request.getSku() + "' en el catálogo", e);
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al crear sneaker con servicio Python en {}: {} - {}",
                    pythonServiceBaseUrl, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Error al crear sneaker en el catálogo: " + e.getMessage(), e);
        } catch (RestClientException e) {
            logger.error("Error de conexión al crear sneaker con servicio Python en {}: {}",
                    pythonServiceBaseUrl, e.getMessage());
            throw new RuntimeException("Error de conexión con el servicio de catálogo", e);
        }
    }

    @Override
    public SearchSneakersResponse searchSneakers(
            String brand,
            String category,
            String gender,
            String collection,
            String search,
            int page,
            int size) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(pythonServiceBaseUrl + "/api/catalog/sneakers");

            if (brand != null && !brand.isBlank()) {
                builder.queryParam("brand", brand);
            }
            if (category != null && !category.isBlank()) {
                builder.queryParam("category", category);
            }
            if (gender != null && !gender.isBlank()) {
                builder.queryParam("gender", gender);
            }
            if (collection != null && !collection.isBlank()) {
                builder.queryParam("collection", collection);
            }
            if (search != null && !search.isBlank()) {
                builder.queryParam("search", search);
            }
            builder.queryParam("page", page);
            builder.queryParam("size", size);

            String url = builder.toUriString();
            logger.debug("Buscando sneakers en servicio Python: {}", url);

            ResponseEntity<SearchSneakersPythonResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<SearchSneakersPythonResponse>() {}
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                SearchSneakersPythonResponse pythonResponse = response.getBody();
                List<SneakerResponse> sneakers = pythonResponse.getSneakers().stream()
                        .map(this::convertToSneakerResponse)
                        .toList();

                return new SearchSneakersResponse(
                        sneakers,
                        pythonResponse.getTotal(),
                        pythonResponse.getPage(),
                        pythonResponse.getSize()
                );
            }

            return new SearchSneakersResponse(List.of(), 0, page, size);
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP al buscar sneakers con servicio Python en {}: {} - {}",
                    pythonServiceBaseUrl, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Error al buscar sneakers en el catálogo: " + e.getMessage(), e);
        } catch (RestClientException e) {
            logger.error("Error de conexión al buscar sneakers con servicio Python en {}: {}",
                    pythonServiceBaseUrl, e.getMessage());
            throw new RuntimeException("Error de conexión con el servicio de catálogo", e);
        }
    }

    private SneakerResponse convertToSneakerResponse(SneakerPythonResponse pythonResponse) {
        SneakerResponse.SneakerMedia media = pythonResponse.getMedia() != null
                ? new SneakerResponse.SneakerMedia(
                        pythonResponse.getMedia().getCoverImage(),
                        pythonResponse.getMedia().getGallery() != null
                                ? pythonResponse.getMedia().getGallery()
                                : List.of())
                : new SneakerResponse.SneakerMedia(null, List.of());

        return new SneakerResponse(
                pythonResponse.getSku(),
                pythonResponse.getBrand(),
                pythonResponse.getModel(),
                pythonResponse.getGender(),
                pythonResponse.getDescription(),
                pythonResponse.getCategories() != null ? pythonResponse.getCategories() : List.of(),
                pythonResponse.getCollections() != null ? pythonResponse.getCollections() : List.of(),
                media
        );
    }

    private SneakerPythonRequest convertToPythonRequest(CreateSneakerRequest request) {
        SneakerPythonRequest pythonRequest = new SneakerPythonRequest();
        pythonRequest.setSku(request.getSku());
        pythonRequest.setBrand(request.getBrand());
        pythonRequest.setModel(request.getModel());
        pythonRequest.setGender(request.getGender());
        pythonRequest.setDescription(request.getDescription());
        pythonRequest.setCategories(request.getCategories());
        pythonRequest.setCollections(request.getCollections());

        if (request.getMedia() != null) {
            SneakerPythonRequest.SneakerMediaRequest media = new SneakerPythonRequest.SneakerMediaRequest();
            media.setCoverImage(request.getMedia().getCoverImage());
            media.setGallery(request.getMedia().getGallery());
            pythonRequest.setMedia(media);
        }

        return pythonRequest;
    }

    /**
     * Clase interna para mapear la respuesta completa de un sneaker desde Python.
     */
    @Getter
    @Setter
    private static class SneakerPythonResponse {
        private String sku;
        private String brand;
        private String model;
        private String gender;
        private String description;
        private List<String> categories;
        private List<String> collections;
        private SneakerMediaResponse media;

        @Getter
        @Setter
        private static class SneakerMediaResponse {
            private String coverImage;
            private List<String> gallery;
        }
    }

    /**
     * Clase interna para mapear la solicitud de creación de sneaker a Python.
     */
    @Getter
    @Setter
    private static class SneakerPythonRequest {
        private String sku;
        private String brand;
        private String model;
        private String gender;
        private String description;
        private List<String> categories;
        private List<String> collections;
        private SneakerMediaRequest media;

        @Getter
        @Setter
        private static class SneakerMediaRequest {
            private String coverImage;
            private List<String> gallery;
        }
    }

    /**
     * Clase interna para mapear la respuesta de búsqueda de sneakers desde Python.
     */
    @Getter
    @Setter
    private static class SearchSneakersPythonResponse {
        private List<SneakerPythonResponse> sneakers;
        private int total;
        private int page;
        private int size;
    }
}
