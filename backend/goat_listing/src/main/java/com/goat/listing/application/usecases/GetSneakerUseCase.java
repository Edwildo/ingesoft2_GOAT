package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.SneakerResponse;
import com.goat.listing.ports.CatalogServicePort;

import java.util.Optional;

/**
 * Caso de uso para obtener un sneaker por su SKU.
 */
public class GetSneakerUseCase {
    private final CatalogServicePort catalogServicePort;

    public GetSneakerUseCase(CatalogServicePort catalogServicePort) {
        this.catalogServicePort = catalogServicePort;
    }

    /**
     * Obtiene un sneaker por su SKU.
     *
     * @param sku SKU del sneaker
     * @return Sneaker encontrado o Optional vacío si no existe
     */
    public Optional<SneakerResponse> execute(String sku) {
        return catalogServicePort.getSneakerBySku(sku);
    }
}

