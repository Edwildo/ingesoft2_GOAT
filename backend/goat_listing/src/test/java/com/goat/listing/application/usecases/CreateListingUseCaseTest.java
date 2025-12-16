package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.CreateListingRequest;
import com.goat.listing.application.dto.ListingResponse;
import com.goat.listing.domain.entities.Listing;
import com.goat.listing.domain.enums.ListingStatus;
import com.goat.listing.ports.CatalogServicePort;
import com.goat.listing.ports.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CreateListingUseCase.
 * Prueba el flujo de creación de listings con diferentes escenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateListingUseCase - Tests Unitarios")
class CreateListingUseCaseTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private CatalogServicePort catalogServicePort;

    @InjectMocks
    private CreateListingUseCase createListingUseCase;

    private UUID sellerId;
    private CreateListingRequest validRequest;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        validRequest = new CreateListingRequest();
        validRequest.setSneakerSku("AIR-JORDAN-1-HIGH-OG-BLACK-WHITE");
        validRequest.setSize("10");
        validRequest.setCondition("NEW");
        validRequest.setGender("MALE");
        validRequest.setBrand("Nike");
        validRequest.setColor("Black/White");
        validRequest.setPrice(new BigDecimal("199.99"));
        validRequest.setCoverImage("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Debería crear listing exitosamente cuando el SKU existe")
    void shouldCreateListingSuccessfullyWhenSkuExists() {
        // Arrange
        when(catalogServicePort.validateSneakerSku(validRequest.getSneakerSku()))
                .thenReturn(true);

        Listing savedListing = new Listing(
                sellerId,
                validRequest.getSneakerSku(),
                validRequest.getSize(),
                validRequest.getCondition(),
                validRequest.getGender(),
                validRequest.getBrand(),
                validRequest.getColor(),
                validRequest.getPrice(),
                validRequest.getCoverImage()
        );
        UUID listingId = UUID.randomUUID();
        savedListing.setId(listingId);

        when(listingRepository.save(any(Listing.class))).thenReturn(savedListing);

        // Act
        ListingResponse response = createListingUseCase.execute(sellerId, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(listingId, response.id());
        assertEquals(sellerId, response.sellerId());
        assertEquals(validRequest.getSneakerSku(), response.sneakerSku());
        assertEquals(validRequest.getSize(), response.size());
        assertEquals(validRequest.getCondition(), response.condition());
        assertEquals(validRequest.getGender(), response.gender());
        assertEquals(validRequest.getBrand(), response.brand());
        assertEquals(validRequest.getColor(), response.color());
        assertEquals(validRequest.getPrice(), response.price());
        assertEquals(validRequest.getCoverImage(), response.coverImage());
        assertEquals(ListingStatus.DRAFT.getCode(), response.status());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        // Verificar que se validó el SKU
        verify(catalogServicePort, times(1)).validateSneakerSku(validRequest.getSneakerSku());

        // Verificar que se guardó el listing con estado DRAFT
        ArgumentCaptor<Listing> listingCaptor = ArgumentCaptor.forClass(Listing.class);
        verify(listingRepository, times(1)).save(listingCaptor.capture());
        Listing capturedListing = listingCaptor.getValue();
        assertEquals(ListingStatus.DRAFT, capturedListing.getStatus());
        assertEquals(sellerId, capturedListing.getSellerId());
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException cuando el SKU no existe")
    void shouldThrowExceptionWhenSkuDoesNotExist() {
        // Arrange
        when(catalogServicePort.validateSneakerSku(validRequest.getSneakerSku()))
                .thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createListingUseCase.execute(sellerId, validRequest)
        );

        assertEquals("El SKU del sneaker no existe en el catálogo", exception.getMessage());

        // Verificar que se validó el SKU pero no se guardó nada
        verify(catalogServicePort, times(1)).validateSneakerSku(validRequest.getSneakerSku());
        verify(listingRepository, never()).save(any(Listing.class));
    }

    @Test
    @DisplayName("Debería crear listing sin imagen de portada cuando coverImage es null")
    void shouldCreateListingWithoutCoverImage() {
        // Arrange
        validRequest.setCoverImage(null);
        when(catalogServicePort.validateSneakerSku(validRequest.getSneakerSku()))
                .thenReturn(true);

        Listing savedListing = new Listing(
                sellerId,
                validRequest.getSneakerSku(),
                validRequest.getSize(),
                validRequest.getCondition(),
                validRequest.getGender(),
                validRequest.getBrand(),
                validRequest.getColor(),
                validRequest.getPrice(),
                null
        );
        savedListing.setId(UUID.randomUUID());

        when(listingRepository.save(any(Listing.class))).thenReturn(savedListing);

        // Act
        ListingResponse response = createListingUseCase.execute(sellerId, validRequest);

        // Assert
        assertNotNull(response);
        assertNull(response.coverImage());
        verify(catalogServicePort, times(1)).validateSneakerSku(validRequest.getSneakerSku());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }

    @Test
    @DisplayName("Debería crear listing con precio mínimo válido (0.01)")
    void shouldCreateListingWithMinimumPrice() {
        // Arrange
        validRequest.setPrice(new BigDecimal("0.01"));
        when(catalogServicePort.validateSneakerSku(validRequest.getSneakerSku()))
                .thenReturn(true);

        Listing savedListing = new Listing(
                sellerId,
                validRequest.getSneakerSku(),
                validRequest.getSize(),
                validRequest.getCondition(),
                validRequest.getGender(),
                validRequest.getBrand(),
                validRequest.getColor(),
                validRequest.getPrice(),
                validRequest.getCoverImage()
        );
        savedListing.setId(UUID.randomUUID());

        when(listingRepository.save(any(Listing.class))).thenReturn(savedListing);

        // Act
        ListingResponse response = createListingUseCase.execute(sellerId, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("0.01"), response.price());
        verify(catalogServicePort, times(1)).validateSneakerSku(validRequest.getSneakerSku());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }

    @Test
    @DisplayName("Debería crear listing con precio alto válido")
    void shouldCreateListingWithHighPrice() {
        // Arrange
        validRequest.setPrice(new BigDecimal("9999.99"));
        when(catalogServicePort.validateSneakerSku(validRequest.getSneakerSku()))
                .thenReturn(true);

        Listing savedListing = new Listing(
                sellerId,
                validRequest.getSneakerSku(),
                validRequest.getSize(),
                validRequest.getCondition(),
                validRequest.getGender(),
                validRequest.getBrand(),
                validRequest.getColor(),
                validRequest.getPrice(),
                validRequest.getCoverImage()
        );
        savedListing.setId(UUID.randomUUID());

        when(listingRepository.save(any(Listing.class))).thenReturn(savedListing);

        // Act
        ListingResponse response = createListingUseCase.execute(sellerId, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("9999.99"), response.price());
        verify(catalogServicePort, times(1)).validateSneakerSku(validRequest.getSneakerSku());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }

    @Test
    @DisplayName("Debería crear listing con strings en límite máximo de caracteres")
    void shouldCreateListingWithMaxLengthStrings() {
        // Arrange
        // SKU máximo: 64 caracteres
        validRequest.setSneakerSku("A".repeat(64));
        // Size máximo: 16 caracteres
        validRequest.setSize("B".repeat(16));
        // Condition máximo: 32 caracteres
        validRequest.setCondition("C".repeat(32));
        // Gender máximo: 16 caracteres
        validRequest.setGender("D".repeat(16));
        // Brand máximo: 64 caracteres
        validRequest.setBrand("E".repeat(64));
        // Color máximo: 32 caracteres
        validRequest.setColor("F".repeat(32));

        when(catalogServicePort.validateSneakerSku(validRequest.getSneakerSku()))
                .thenReturn(true);

        Listing savedListing = new Listing(
                sellerId,
                validRequest.getSneakerSku(),
                validRequest.getSize(),
                validRequest.getCondition(),
                validRequest.getGender(),
                validRequest.getBrand(),
                validRequest.getColor(),
                validRequest.getPrice(),
                validRequest.getCoverImage()
        );
        savedListing.setId(UUID.randomUUID());

        when(listingRepository.save(any(Listing.class))).thenReturn(savedListing);

        // Act
        ListingResponse response = createListingUseCase.execute(sellerId, validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(64, response.sneakerSku().length());
        assertEquals(16, response.size().length());
        assertEquals(32, response.condition().length());
        assertEquals(16, response.gender().length());
        assertEquals(64, response.brand().length());
        assertEquals(32, response.color().length());
        verify(catalogServicePort, times(1)).validateSneakerSku(validRequest.getSneakerSku());
        verify(listingRepository, times(1)).save(any(Listing.class));
    }
}
