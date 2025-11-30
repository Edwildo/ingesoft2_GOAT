package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.CreateSneakerRequest;
import com.goat.listing.application.dto.SneakerResponse;
import com.goat.listing.ports.CatalogServicePort;

/**
 * Caso de uso para crear un nuevo sneaker en el catálogo.
 */
public class CreateSneakerUseCase {
    private final CatalogServicePort catalogServicePort;

    public CreateSneakerUseCase(CatalogServicePort catalogServicePort) {
        this.catalogServicePort = catalogServicePort;
    }

    /**
     * Crea un nuevo sneaker en el catálogo de Python.
     *
     * @param request Datos del sneaker a crear
     * @return Sneaker creado
     */
    public SneakerResponse execute(CreateSneakerRequest request) {
        return catalogServicePort.createSneaker(request);
    }
}

