package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.SearchSneakersResponse;
import com.goat.listing.ports.CatalogServicePort;

/**
 * Caso de uso para buscar sneakers con filtros.
 */
public class SearchSneakersUseCase {
    private final CatalogServicePort catalogServicePort;

    public SearchSneakersUseCase(CatalogServicePort catalogServicePort) {
        this.catalogServicePort = catalogServicePort;
    }

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
    public SearchSneakersResponse execute(
            String brand,
            String category,
            String gender,
            String collection,
            String search,
            int page,
            int size) {
        return catalogServicePort.searchSneakers(
                brand, category, gender, collection, search, page, size);
    }
}

