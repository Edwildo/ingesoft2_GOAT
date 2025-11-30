package com.goat.listing.entrypoints.rest;

import com.goat.listing.application.dto.CreateSneakerRequest;
import com.goat.listing.application.dto.SearchSneakersResponse;
import com.goat.listing.application.dto.SneakerResponse;
import com.goat.listing.application.usecases.CreateSneakerUseCase;
import com.goat.listing.application.usecases.GetSneakerUseCase;
import com.goat.listing.application.usecases.SearchSneakersUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador REST para operaciones de catálogo (SKUs/Sneakers).
 * Entrypoint de la arquitectura hexagonal para operaciones con el catálogo.
 */
@RestController
@RequestMapping("/api/catalog")
@CrossOrigin(origins = "*")
public class CatalogController {
    private final GetSneakerUseCase getSneakerUseCase;
    private final CreateSneakerUseCase createSneakerUseCase;
    private final SearchSneakersUseCase searchSneakersUseCase;

    public CatalogController(
            GetSneakerUseCase getSneakerUseCase,
            CreateSneakerUseCase createSneakerUseCase,
            SearchSneakersUseCase searchSneakersUseCase) {
        this.getSneakerUseCase = getSneakerUseCase;
        this.createSneakerUseCase = createSneakerUseCase;
        this.searchSneakersUseCase = searchSneakersUseCase;
    }

    /**
     * Obtiene un sneaker por su SKU.
     * Endpoint público - no requiere autenticación.
     */
    @GetMapping("/sneakers/{sku}")
    public ResponseEntity<SneakerResponse> getSneakerBySku(@PathVariable String sku) {
        Optional<SneakerResponse> sneaker = getSneakerUseCase.execute(sku);
        
        if (sneaker.isPresent()) {
            return ResponseEntity.ok(sneaker.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca sneakers con filtros opcionales.
     * Endpoint público - no requiere autenticación.
     */
    @GetMapping("/sneakers")
    public ResponseEntity<SearchSneakersResponse> searchSneakers(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String collection,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        SearchSneakersResponse response = searchSneakersUseCase.execute(
                brand, category, gender, collection, search, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo sneaker en el catálogo.
     * Requiere autenticación (para evitar spam).
     */
    @PostMapping("/sneakers")
    public ResponseEntity<SneakerResponse> createSneaker(
            @Valid @RequestBody CreateSneakerRequest request) {
        SneakerResponse response = createSneakerUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

