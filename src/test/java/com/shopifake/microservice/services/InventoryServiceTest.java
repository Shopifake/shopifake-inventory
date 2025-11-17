package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.AdjustInventoryRequest;
import com.shopifake.microservice.dtos.CreateInventoryRequest;
import com.shopifake.microservice.entities.InventoryItem;
import com.shopifake.microservice.entities.InventoryStatus;
import com.shopifake.microservice.repositories.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InventoryService}.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private CreateInventoryRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = CreateInventoryRequest.builder()
                .productId(UUID.randomUUID())
                .initialQuantity(10)
                .build();
    }

    @Test
    @DisplayName("Should create inventory when none exists")
    void shouldCreateInventory() {
        when(inventoryRepository.existsByProductId(createRequest.getProductId())).thenReturn(false);
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> {
            InventoryItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            item.setCreatedAt(LocalDateTime.now());
            item.setUpdatedAt(LocalDateTime.now());
            return item;
        });

        var response = inventoryService.createInventory(createRequest);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(InventoryStatus.IN_STOCK);
        ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(inventoryRepository).save(captor.capture());
        assertThat(captor.getValue().getAvailableQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should prevent duplicate inventory rows")
    void shouldRejectDuplicateInventory() {
        when(inventoryRepository.existsByProductId(createRequest.getProductId())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.createInventory(createRequest));

        assertThat(exception.getMessage()).contains("Inventory already exists");
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject adjustments that go below zero")
    void shouldRejectNegativeAdjustment() {
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID())
                .productId(createRequest.getProductId())
                .availableQuantity(1)
                .status(InventoryStatus.IN_STOCK)
                .build();
        when(inventoryRepository.findByProductId(item.getProductId())).thenReturn(Optional.of(item));

        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .quantityDelta(-2)
                .reason("Manual adjustment")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.adjustInventory(item.getProductId(), request));

        assertThat(exception.getMessage()).contains("negative quantity");
    }
}


