package com.goat.listing.ports;

import com.goat.listing.application.dto.SneakerResponse;
import com.goat.listing.application.dto.CreateSneakerRequest;
import com.goat.listing.application.dto.SearchSneakersResponse;

import java.util.Optional;

/**
 * Puerto (interfaz) para operaciones con el servicio Python Catalog.
 * Define las operaciones sin depender de implementación.
 */
public interface CatalogServicePort {
    /**
     * Valida que un SKU de sneaker existe en el catálogo de Python.
     *
     * @param sku SKU del sneaker a validar
     * @return true si el SKU existe, false en caso contrario
     */
    boolean validateSneakerSku(String sku);

    /**
     * Obtiene un sneaker por su SKU.
     *
     * @param sku SKU del sneaker
     * @return Sneaker encontrado o Optional vacío si no existe
     */
    Optional<SneakerResponse> getSneakerBySku(String sku);

    /**
     * Crea un nuevo sneaker en el catálogo de Python.
     *
     * @param request Datos del sneaker a crear
     * @return Sneaker creado
     */
    SneakerResponse createSneaker(CreateSneakerRequest request);

    /**
     * Busca sneakers con filtros opcionales.
     *
     * @param brand Marca (opcional)
     * @param category Categoría (opcional)
     * @param gender Género (opcional)
     * @param collection Colección (opcional)
     * @param search Texto de búsqueda (opcional)
     * @param page Número de página (default: 1)
     * @param size Tamaño de página (default: 20)
     * @return Respuesta con sneakers encontrados
     */
    SearchSneakersResponse searchSneakers(
            String brand,
            String category,
            String gender,
            String collection,
            String search,
            int page,
            int size
    );
}
