package com.goat.listing.entrypoints.rest;

import com.goat.listing.application.dto.CreateListingRequest;
import com.goat.listing.application.dto.ListingListResponse;
import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.application.dto.UpdateListingRequest;
import com.goat.listing.application.usecases.ArchiveListingUseCase;
import com.goat.listing.application.usecases.CreateListingUseCase;
import com.goat.listing.application.usecases.GetListingUseCase;
import com.goat.listing.application.usecases.GetListingsUseCase;
import com.goat.listing.application.usecases.GetMyListingsUseCase;
import com.goat.listing.application.usecases.PublishListingUseCase;
import com.goat.listing.application.usecases.UpdateListingUseCase;
import com.goat.listing.domain.enums.ListingStatus;
import com.goat.listing.domain.exceptions.InsufficientRoleException;
import com.goat.identity.infrastructure.security.SecurityContextHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para operaciones de listings.
 * Entrypoint de la arquitectura hexagonal para Listings.
 */
@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = "*")
public class ListingController {
    private final CreateListingUseCase createListingUseCase;
    private final UpdateListingUseCase updateListingUseCase;
    private final PublishListingUseCase publishListingUseCase;
    private final ArchiveListingUseCase archiveListingUseCase;
    private final GetListingUseCase getListingUseCase;
    private final GetListingsUseCase getListingsUseCase;
    private final GetMyListingsUseCase getMyListingsUseCase;
    private final SecurityContextHelper securityContextHelper;

    public ListingController(
            CreateListingUseCase createListingUseCase,
            UpdateListingUseCase updateListingUseCase,
            PublishListingUseCase publishListingUseCase,
            ArchiveListingUseCase archiveListingUseCase,
            GetListingUseCase getListingUseCase,
            GetListingsUseCase getListingsUseCase,
            GetMyListingsUseCase getMyListingsUseCase,
            SecurityContextHelper securityContextHelper) {
        this.createListingUseCase = createListingUseCase;
        this.updateListingUseCase = updateListingUseCase;
        this.publishListingUseCase = publishListingUseCase;
        this.archiveListingUseCase = archiveListingUseCase;
        this.getListingUseCase = getListingUseCase;
        this.getListingsUseCase = getListingsUseCase;
        this.getMyListingsUseCase = getMyListingsUseCase;
        this.securityContextHelper = securityContextHelper;
    }

    /**
     * Obtiene listings públicos con filtros y paginación.
     * Endpoint público - no requiere autenticación.
     */
    @GetMapping
    public ResponseEntity<ListingListResponse> getListings(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        ListingListResponse response = getListingsUseCase.execute(
                brand, size, condition, gender, color,
                minPrice, maxPrice, page, pageSize
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un listing por su ID.
     * Endpoint público - solo muestra listings PUBLISHED.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ListingResponse> getListing(@PathVariable UUID id) {
        ListingResponse response = getListingUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene los listings del seller autenticado.
     * Requiere autenticación y rol SELLER.
     * El sellerId se obtiene automáticamente del SecurityContext.
     */
    @GetMapping("/mine")
    public ResponseEntity<ListingListResponse> getMyListings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        
        validateSellerRole();
        UUID sellerId = securityContextHelper.getCurrentUserId();
        
        ListingStatus listingStatus = status != null ? ListingStatus.fromCode(status) : null;
        ListingListResponse response = getMyListingsUseCase.execute(sellerId, listingStatus, page, pageSize);
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo listing en estado DRAFT.
     * Requiere autenticación y rol SELLER.
     */
    @PostMapping
    public ResponseEntity<ListingResponse> createListing(
            @Valid @RequestBody CreateListingRequest request) {
        
        validateSellerRole();
        UUID sellerId = securityContextHelper.getCurrentUserId();
        ListingResponse response = createListingUseCase.execute(sellerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualiza un listing existente (solo DRAFT).
     * Requiere autenticación, rol SELLER y ser dueño del listing.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ListingResponse> updateListing(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateListingRequest request) {
        
        validateSellerRole();
        UUID sellerId = securityContextHelper.getCurrentUserId();
        ListingResponse response = updateListingUseCase.execute(id, sellerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Publica un listing (DRAFT -> PUBLISHED).
     * Requiere autenticación, rol SELLER y ser dueño del listing.
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<ListingResponse> publishListing(@PathVariable UUID id) {
        validateSellerRole();
        UUID sellerId = securityContextHelper.getCurrentUserId();
        ListingResponse response = publishListingUseCase.execute(id, sellerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Archiva un listing (PUBLISHED -> ARCHIVED).
     * Requiere autenticación, rol SELLER y ser dueño del listing.
     */
    @PutMapping("/{id}/archive")
    public ResponseEntity<ListingResponse> archiveListing(@PathVariable UUID id) {
        validateSellerRole();
        UUID sellerId = securityContextHelper.getCurrentUserId();
        ListingResponse response = archiveListingUseCase.execute(id, sellerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Valida que el usuario autenticado tenga el rol SELLER.
     * Lanza InsufficientRoleException si no tiene el rol requerido.
     */
    private void validateSellerRole() {
        if (!securityContextHelper.hasRole("SELLER")) {
            throw new InsufficientRoleException("Se requiere el rol SELLER para realizar esta operación");
        }
    }
}
