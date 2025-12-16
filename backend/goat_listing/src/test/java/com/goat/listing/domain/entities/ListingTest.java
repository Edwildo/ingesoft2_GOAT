package com.goat.listing.domain.entities;

import com.goat.listing.domain.enums.ListingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad Listing.
 * Prueba las reglas de negocio y transiciones de estado.
 */
@DisplayName("Listing Entity - Tests Unitarios")
class ListingTest {

    private Listing listing;
    private UUID sellerId;
    private UUID listingId;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        listingId = UUID.randomUUID();
        listing = new Listing(
                sellerId,
                "AIR-JORDAN-1-HIGH-OG",
                "10",
                "NEW",
                "MALE",
                "Nike",
                "Black/White",
                new BigDecimal("199.99"),
                "https://example.com/image.jpg"
        );
        listing.setId(listingId);
    }

    @Test
    @DisplayName("Debería crear listing en estado DRAFT por defecto")
    void shouldCreateListingWithDraftStatus() {
        // Assert
        assertEquals(ListingStatus.DRAFT, listing.getStatus());
        assertNotNull(listing.getCreatedAt());
        assertNotNull(listing.getUpdatedAt());
    }

    @Test
    @DisplayName("Debería permitir editar listing en estado DRAFT")
    void shouldAllowEditWhenDraft() {
        // Arrange
        listing.setStatus(ListingStatus.DRAFT);

        // Act & Assert
        assertTrue(listing.canBeEdited());
    }

    @Test
    @DisplayName("Debería permitir editar listing en estado ARCHIVED")
    void shouldAllowEditWhenArchived() {
        // Arrange
        listing.setStatus(ListingStatus.ARCHIVED);

        // Act & Assert
        assertTrue(listing.canBeEdited());
    }

    @Test
    @DisplayName("No debería permitir editar listing en estado PUBLISHED")
    void shouldNotAllowEditWhenPublished() {
        // Arrange
        listing.setStatus(ListingStatus.PUBLISHED);

        // Act & Assert
        assertFalse(listing.canBeEdited());
    }

    @Test
    @DisplayName("Debería permitir publicar listing en estado DRAFT")
    void shouldAllowPublishWhenDraft() {
        // Arrange
        listing.setStatus(ListingStatus.DRAFT);

        // Act & Assert
        assertTrue(listing.canBePublished());
    }

    @Test
    @DisplayName("Debería permitir publicar listing en estado ARCHIVED")
    void shouldAllowPublishWhenArchived() {
        // Arrange
        listing.setStatus(ListingStatus.ARCHIVED);

        // Act & Assert
        assertTrue(listing.canBePublished());
    }

    @Test
    @DisplayName("No debería permitir publicar listing en estado PUBLISHED")
    void shouldNotAllowPublishWhenPublished() {
        // Arrange
        listing.setStatus(ListingStatus.PUBLISHED);

        // Act & Assert
        assertFalse(listing.canBePublished());
    }

    @Test
    @DisplayName("Debería permitir archivar listing en estado PUBLISHED")
    void shouldAllowArchiveWhenPublished() {
        // Arrange
        listing.setStatus(ListingStatus.PUBLISHED);

        // Act & Assert
        assertTrue(listing.canBeArchived());
    }

    @Test
    @DisplayName("No debería permitir archivar listing en estado DRAFT")
    void shouldNotAllowArchiveWhenDraft() {
        // Arrange
        listing.setStatus(ListingStatus.DRAFT);

        // Act & Assert
        assertFalse(listing.canBeArchived());
    }

    @Test
    @DisplayName("No debería permitir archivar listing en estado ARCHIVED")
    void shouldNotAllowArchiveWhenArchived() {
        // Arrange
        listing.setStatus(ListingStatus.ARCHIVED);

        // Act & Assert
        assertFalse(listing.canBeArchived());
    }

    @Test
    @DisplayName("Debería cambiar estado de DRAFT a PUBLISHED al publicar")
    void shouldChangeStatusFromDraftToPublished() {
        // Arrange
        listing.setStatus(ListingStatus.DRAFT);
        LocalDateTime beforeUpdate = listing.getUpdatedAt();

        // Act
        listing.publish();

        // Assert
        assertEquals(ListingStatus.PUBLISHED, listing.getStatus());
        assertTrue(listing.getUpdatedAt().isAfter(beforeUpdate) || 
                   listing.getUpdatedAt().equals(beforeUpdate));
    }

    @Test
    @DisplayName("Debería cambiar estado de ARCHIVED a PUBLISHED al publicar")
    void shouldChangeStatusFromArchivedToPublished() {
        // Arrange
        listing.setStatus(ListingStatus.ARCHIVED);
        LocalDateTime beforeUpdate = listing.getUpdatedAt();

        // Act
        listing.publish();

        // Assert
        assertEquals(ListingStatus.PUBLISHED, listing.getStatus());
        assertTrue(listing.getUpdatedAt().isAfter(beforeUpdate) || 
                   listing.getUpdatedAt().equals(beforeUpdate));
    }

    @Test
    @DisplayName("Debería lanzar excepción al intentar publicar listing PUBLISHED")
    void shouldThrowExceptionWhenPublishingPublishedListing() {
        // Arrange
        listing.setStatus(ListingStatus.PUBLISHED);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listing.publish()
        );

        assertEquals("Solo listings en estado DRAFT o ARCHIVED pueden publicarse", 
                     exception.getMessage());
        assertEquals(ListingStatus.PUBLISHED, listing.getStatus());
    }

    @Test
    @DisplayName("Debería cambiar estado de PUBLISHED a ARCHIVED al archivar")
    void shouldChangeStatusFromPublishedToArchived() {
        // Arrange
        listing.setStatus(ListingStatus.PUBLISHED);
        LocalDateTime beforeUpdate = listing.getUpdatedAt();

        // Act
        listing.archive();

        // Assert
        assertEquals(ListingStatus.ARCHIVED, listing.getStatus());
        assertTrue(listing.getUpdatedAt().isAfter(beforeUpdate) || 
                   listing.getUpdatedAt().equals(beforeUpdate));
    }

    @Test
    @DisplayName("Debería lanzar excepción al intentar archivar listing DRAFT")
    void shouldThrowExceptionWhenArchivingDraftListing() {
        // Arrange
        listing.setStatus(ListingStatus.DRAFT);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listing.archive()
        );

        assertEquals("Solo listings en estado PUBLISHED pueden archivarse", 
                     exception.getMessage());
        assertEquals(ListingStatus.DRAFT, listing.getStatus());
    }

    @Test
    @DisplayName("Debería lanzar excepción al intentar archivar listing ARCHIVED")
    void shouldThrowExceptionWhenArchivingArchivedListing() {
        // Arrange
        listing.setStatus(ListingStatus.ARCHIVED);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> listing.archive()
        );

        assertEquals("Solo listings en estado PUBLISHED pueden archivarse", 
                     exception.getMessage());
        assertEquals(ListingStatus.ARCHIVED, listing.getStatus());
    }

    @Test
    @DisplayName("Debería actualizar updatedAt al cambiar estado")
    void shouldUpdateUpdatedAtWhenChangingStatus() throws InterruptedException {
        // Arrange
        listing.setStatus(ListingStatus.DRAFT);
        LocalDateTime initialUpdatedAt = listing.getUpdatedAt();
        
        // Pequeña pausa para asegurar diferencia de tiempo
        Thread.sleep(10);

        // Act
        listing.publish();

        // Assert
        assertTrue(listing.getUpdatedAt().isAfter(initialUpdatedAt));
    }

    @Test
    @DisplayName("Debería comparar listings por ID en equals")
    void shouldCompareListingsByIdInEquals() {
        // Arrange
        Listing listing1 = new Listing(
                sellerId, "SKU-1", "10", "NEW", "MALE", "Nike", "Black", 
                new BigDecimal("100"), null
        );
        listing1.setId(listingId);

        Listing listing2 = new Listing(
                sellerId, "SKU-2", "11", "USED", "FEMALE", "Adidas", "White", 
                new BigDecimal("200"), null
        );
        listing2.setId(listingId); // Mismo ID

        Listing listing3 = new Listing(
                sellerId, "SKU-1", "10", "NEW", "MALE", "Nike", "Black", 
                new BigDecimal("100"), null
        );
        listing3.setId(UUID.randomUUID()); // Diferente ID

        // Act & Assert
        assertEquals(listing1, listing2); // Mismo ID
        assertNotEquals(listing1, listing3); // Diferente ID
    }

    @Test
    @DisplayName("Debería usar ID en hashCode")
    void shouldUseIdInHashCode() {
        // Arrange
        Listing listing1 = new Listing(
                sellerId, "SKU-1", "10", "NEW", "MALE", "Nike", "Black", 
                new BigDecimal("100"), null
        );
        listing1.setId(listingId);

        Listing listing2 = new Listing(
                sellerId, "SKU-2", "11", "USED", "FEMALE", "Adidas", "White", 
                new BigDecimal("200"), null
        );
        listing2.setId(listingId); // Mismo ID

        // Act & Assert
        assertEquals(listing1.hashCode(), listing2.hashCode());
    }
}
