package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.AdjustInventoryRequest;
import com.shopifake.microservice.dtos.CreateInventoryRequest;
import com.shopifake.microservice.dtos.InventoryResponse;
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
import java.util.List;
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

    @Test
    @DisplayName("Should return inventory by product id")
    void shouldGetInventory() {
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID())
                .productId(createRequest.getProductId())
                .availableQuantity(5)
                .status(InventoryStatus.IN_STOCK)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(inventoryRepository.findByProductId(item.getProductId())).thenReturn(Optional.of(item));

        InventoryResponse response = inventoryService.getInventory(item.getProductId());

        assertThat(response.getProductId()).isEqualTo(item.getProductId());
        assertThat(response.getAvailableQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should list inventory by status")
    void shouldListInventoryByStatus() {
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID())
                .productId(createRequest.getProductId())
                .availableQuantity(0)
                .status(InventoryStatus.OUT_OF_STOCK)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(inventoryRepository.findByStatus(InventoryStatus.OUT_OF_STOCK))
                .thenReturn(List.of(item));

        List<InventoryResponse> responses = inventoryService.listInventory("OUT_OF_STOCK");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo(InventoryStatus.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("Should delete inventory when present")
    void shouldDeleteInventory() {
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID())
                .productId(createRequest.getProductId())
                .availableQuantity(2)
                .status(InventoryStatus.IN_STOCK)
                .build();
        when(inventoryRepository.findByProductId(item.getProductId())).thenReturn(Optional.of(item));

        inventoryService.deleteInventory(item.getProductId());

        verify(inventoryRepository).deleteById(item.getId());
    }

    @Test
    @DisplayName("Should increase inventory and set status to IN_STOCK")
    void shouldIncreaseInventory() {
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID())
                .productId(createRequest.getProductId())
                .availableQuantity(0)
                .status(InventoryStatus.OUT_OF_STOCK)
                .build();
        when(inventoryRepository.findByProductId(item.getProductId())).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .quantityDelta(5)
                .reason("Replenishment")
                .build();

        InventoryResponse response = inventoryService.adjustInventory(item.getProductId(), request);

        assertThat(response.getAvailableQuantity()).isEqualTo(5);
        assertThat(response.getStatus()).isEqualTo(InventoryStatus.IN_STOCK);
    }
}


