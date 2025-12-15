package com.goat.listing.application.usecases;

import com.goat.listing.domain.entities.Listing;
import com.goat.listing.ports.ListingRepository;
import com.goat.order.adapters.persistence.repository.OrderItemJpaRepository;

import java.util.UUID;

/**
 * Caso de uso para eliminar un listing.
 * Solo permite eliminar listings en estado DRAFT o ARCHIVED.
 * Los listings PUBLISHED deben archivarse primero.
 * No permite eliminar listings que tienen órdenes asociadas.
 */
public class DeleteListingUseCase {
    private final ListingRepository listingRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    public DeleteListingUseCase(
            ListingRepository listingRepository,
            OrderItemJpaRepository orderItemJpaRepository) {
        this.listingRepository = listingRepository;
        this.orderItemJpaRepository = orderItemJpaRepository;
    }

    /**
     * Elimina un listing.
     * Valida que el listing pertenezca al seller y esté en estado DRAFT o ARCHIVED.
     *
     * @param listingId ID del listing a eliminar
     * @param sellerId ID del seller que elimina (para validar ownership)
     * @throws IllegalArgumentException si el listing no existe
     * @throws IllegalStateException si el listing no puede ser eliminado (está PUBLISHED)
     */
    public void execute(UUID listingId, UUID sellerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing no encontrado"));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("No tienes permiso para eliminar este listing");
        }

        // Solo se pueden eliminar listings en estado DRAFT o ARCHIVED
        if (listing.getStatus().getCode().equals("PUBLISHED")) {
            throw new IllegalStateException(
                    "No se puede eliminar un listing publicado. " +
                    "Debes archivarlo primero antes de eliminarlo."
            );
        }

        // Verificar si hay órdenes asociadas a este listing
        long orderItemsCount = orderItemJpaRepository.countByListingId(listingId);
        if (orderItemsCount > 0) {
            throw new IllegalStateException(
                    String.format(
                            "No se puede eliminar el listing porque tiene %d orden(es) asociada(s). " +
                            "Los listings con órdenes no pueden ser eliminados para mantener la integridad de los datos.",
                            orderItemsCount
                    )
            );
        }

        listingRepository.deleteById(listingId);
    }
}
