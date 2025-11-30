package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.AdjustInventoryRequest;
import com.shopifake.microservice.dtos.CreateInventoryRequest;
import com.shopifake.microservice.dtos.InventoryResponse;
import com.shopifake.microservice.entities.InventoryItem;
import com.shopifake.microservice.entities.InventoryStatus;
import com.shopifake.microservice.repositories.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Business operations for product inventory tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final Clock clock = Clock.systemUTC();

    /**
     * Create a new inventory row when a product is onboarded.
     */
    @Transactional
    public InventoryResponse createInventory(final CreateInventoryRequest request) {
        if (inventoryRepository.existsByProductId(request.getProductId())) {
            throw new IllegalArgumentException("Inventory already exists for product " + request.getProductId());
        }
        InventoryItem item = InventoryItem.builder()
                .productId(request.getProductId())
                .availableQuantity(request.getInitialQuantity())
                .status(deriveStatus(request.getInitialQuantity()))
                .build();

        InventoryItem saved = inventoryRepository.save(item);
        return mapToResponse(saved);
    }

    /**
     * Get inventory details by product id.
     */
    public InventoryResponse getInventory(final UUID productId) {
        return mapToResponse(getInventoryOrThrow(productId));
    }

    /**
     * List inventory rows optionally filtered by status.
     */
    public List<InventoryResponse> listInventory(final String status) {
        List<InventoryItem> items;
        if (StringUtils.hasText(status)) {
            items = inventoryRepository.findByStatus(parseStatus(status));
        } else {
            items = inventoryRepository.findAll();
        }
        return items.stream().map(this::mapToResponse).toList();
    }

    /**
     * Adjust the on-hand quantity for a product.
     */
    @Transactional
    public InventoryResponse adjustInventory(final UUID productId, final AdjustInventoryRequest request) {
        if (request.getQuantityDelta() == 0) {
            throw new IllegalArgumentException("quantityDelta must be non-zero");
        }
        InventoryItem item = getInventoryOrThrow(productId);
        int newQuantity = item.getAvailableQuantity() + request.getQuantityDelta();
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Adjustment would produce negative quantity");
        }
        item.setAvailableQuantity(newQuantity);
        item.setStatus(deriveStatus(newQuantity));
        if (request.getQuantityDelta() > 0) {
            item.setReplenishmentAt(LocalDateTime.now(clock));
        }
        InventoryItem saved = inventoryRepository.save(item);
        log.info("Adjusted inventory for {} by {} ({})", productId, request.getQuantityDelta(), request.getReason());
        return mapToResponse(saved);
    }

    /**
     * Remove inventory tracking for a product.
     */
    @Transactional
    public void deleteInventory(final UUID productId) {
        InventoryItem item = getInventoryOrThrow(productId);
        inventoryRepository.deleteById(item.getId());
    }

    private InventoryItem getInventoryOrThrow(final UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product " + productId));
    }

    private InventoryStatus deriveStatus(final int availableQuantity) {
        if (availableQuantity <= 0) {
            return InventoryStatus.OUT_OF_STOCK;
        }
        return InventoryStatus.IN_STOCK;
    }

    private InventoryStatus parseStatus(final String status) {
        try {
            return InventoryStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid inventory status: " + status);
        }
    }

    private InventoryResponse mapToResponse(final InventoryItem item) {
        return InventoryResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .availableQuantity(item.getAvailableQuantity())
                .status(item.getStatus())
                .replenishmentAt(item.getReplenishmentAt())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}


