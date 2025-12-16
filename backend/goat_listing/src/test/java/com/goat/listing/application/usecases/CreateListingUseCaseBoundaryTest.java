package com.goat.listing.application.usecases;

import com.goat.listing.application.dto.CreateListingRequest;
import com.goat.listing.ports.CatalogServicePort;
import com.goat.listing.ports.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests de boundary (límites) para CreateListingUseCase.
 * Prueba valores límite, casos extremos y validaciones de boundary.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateListingUseCase - Tests de Boundary")
class CreateListingUseCaseBoundaryTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private CatalogServicePort catalogServicePort;

    @InjectMocks
    private CreateListingUseCase createListingUseCase;

    private UUID sellerId;
    private CreateListingRequest request;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        request = new CreateListingRequest();
        request.setSneakerSku("VALID-SKU-123");
        request.setSize("10");
        request.setCondition("NEW");
        request.setGender("MALE");
        request.setBrand("Nike");
        request.setColor("Black");
        request.setPrice(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Debería rechazar SKU vacío (boundary: string vacío)")
    void shouldRejectEmptySku() {
        // Arrange
        request.setSneakerSku("");
        when(catalogServicePort.validateSneakerSku("")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createListingUseCase.execute(sellerId, request)
        );

        assertEquals("El SKU del sneaker no existe en el catálogo", exception.getMessage());
        verify(catalogServicePort, times(1)).validateSneakerSku("");
        verify(listingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería rechazar SKU null (boundary: null)")
    void shouldRejectNullSku() {
        // Arrange
        request.setSneakerSku(null);
        when(catalogServicePort.validateSneakerSku(null)).thenReturn(false);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> createListingUseCase.execute(sellerId, request)
        );

        verify(catalogServicePort, times(1)).validateSneakerSku(null);
        verify(listingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería aceptar SKU de exactamente 64 caracteres (boundary: máximo)")
    void shouldAcceptSkuWithExactly64Characters() {
        // Arrange
        String maxLengthSku = "A".repeat(64);
        request.setSneakerSku(maxLengthSku);
        when(catalogServicePort.validateSneakerSku(maxLengthSku)).thenReturn(true);
        when(listingRepository.save(any())).thenAnswer(invocation -> {
            com.goat.listing.domain.entities.Listing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        // Act
        assertDoesNotThrow(() -> createListingUseCase.execute(sellerId, request));

        // Assert
        verify(catalogServicePort, times(1)).validateSneakerSku(maxLengthSku);
        verify(listingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería aceptar precio de exactamente 0.01 (boundary: mínimo)")
    void shouldAcceptMinimumPrice() {
        // Arrange
        request.setPrice(new BigDecimal("0.01"));
        when(catalogServicePort.validateSneakerSku(request.getSneakerSku())).thenReturn(true);
        when(listingRepository.save(any())).thenAnswer(invocation -> {
            com.goat.listing.domain.entities.Listing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        // Act
        assertDoesNotThrow(() -> createListingUseCase.execute(sellerId, request));

        // Assert
        verify(catalogServicePort, times(1)).validateSneakerSku(request.getSneakerSku());
        verify(listingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería aceptar precio muy alto (boundary: máximo práctico)")
    void shouldAcceptVeryHighPrice() {
        // Arrange
        request.setPrice(new BigDecimal("999999.99"));
        when(catalogServicePort.validateSneakerSku(request.getSneakerSku())).thenReturn(true);
        when(listingRepository.save(any())).thenAnswer(invocation -> {
            com.goat.listing.domain.entities.Listing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        // Act
        assertDoesNotThrow(() -> createListingUseCase.execute(sellerId, request));

        // Assert
        verify(catalogServicePort, times(1)).validateSneakerSku(request.getSneakerSku());
        verify(listingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería aceptar precio con muchos decimales")
    void shouldAcceptPriceWithManyDecimals() {
        // Arrange
        request.setPrice(new BigDecimal("123.456789"));
        when(catalogServicePort.validateSneakerSku(request.getSneakerSku())).thenReturn(true);
        when(listingRepository.save(any())).thenAnswer(invocation -> {
            com.goat.listing.domain.entities.Listing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        // Act
        assertDoesNotThrow(() -> createListingUseCase.execute(sellerId, request));

        // Assert
        verify(catalogServicePort, times(1)).validateSneakerSku(request.getSneakerSku());
        verify(listingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería aceptar strings de tamaño mínimo (1 carácter)")
    void shouldAcceptMinimumLengthStrings() {
        // Arrange
        request.setSize("1");
        request.setCondition("N");
        request.setGender("M");
        request.setBrand("N");
        request.setColor("B");
        when(catalogServicePort.validateSneakerSku(request.getSneakerSku())).thenReturn(true);
        when(listingRepository.save(any())).thenAnswer(invocation -> {
            com.goat.listing.domain.entities.Listing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        // Act
        assertDoesNotThrow(() -> createListingUseCase.execute(sellerId, request));

        // Assert
        verify(catalogServicePort, times(1)).validateSneakerSku(request.getSneakerSku());
        verify(listingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería aceptar strings en límite máximo para cada campo")
    void shouldAcceptMaxLengthStringsForAllFields() {
        // Arrange
        request.setSneakerSku("A".repeat(64));  // max 64
        request.setSize("B".repeat(16));        // max 16
        request.setCondition("C".repeat(32));   // max 32
        request.setGender("D".repeat(16));       // max 16
        request.setBrand("E".repeat(64));        // max 64
        request.setColor("F".repeat(32));        // max 32

        when(catalogServicePort.validateSneakerSku(request.getSneakerSku())).thenReturn(true);
        when(listingRepository.save(any())).thenAnswer(invocation -> {
            com.goat.listing.domain.entities.Listing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        // Act
        assertDoesNotThrow(() -> createListingUseCase.execute(sellerId, request));

        // Assert
        verify(catalogServicePort, times(1)).validateSneakerSku(request.getSneakerSku());
        verify(listingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería manejar URL de imagen muy larga")
    void shouldHandleVeryLongImageUrl() {
        // Arrange
        String longUrl = "https://example.com/" + "image".repeat(100) + ".jpg";
        request.setCoverImage(longUrl);
        when(catalogServicePort.validateSneakerSku(request.getSneakerSku())).thenReturn(true);
        when(listingRepository.save(any())).thenAnswer(invocation -> {
            com.goat.listing.domain.entities.Listing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        // Act
        assertDoesNotThrow(() -> createListingUseCase.execute(sellerId, request));

        // Assert
        verify(catalogServicePort, times(1)).validateSneakerSku(request.getSneakerSku());
        verify(listingRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Debería manejar múltiples llamadas al servicio de catálogo correctamente")
    void shouldHandleMultipleCatalogServiceCalls() {
        // Arrange
        when(catalogServicePort.validateSneakerSku(request.getSneakerSku())).thenReturn(true);
        when(listingRepository.save(any())).thenAnswer(invocation -> {
            com.goat.listing.domain.entities.Listing listing = invocation.getArgument(0);
            listing.setId(UUID.randomUUID());
            return listing;
        });

        // Act - Crear múltiples listings
        for (int i = 0; i < 5; i++) {
            createListingUseCase.execute(sellerId, request);
        }

        // Assert
        verify(catalogServicePort, times(5)).validateSneakerSku(request.getSneakerSku());
        verify(listingRepository, times(5)).save(any());
    }
}
