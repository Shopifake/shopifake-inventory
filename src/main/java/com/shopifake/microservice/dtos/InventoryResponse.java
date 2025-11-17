package com.shopifake.microservice.dtos;

import com.shopifake.microservice.entities.InventoryStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO returned for inventory reads.
 */
@Value
@Builder
public class InventoryResponse {

    UUID id;

    UUID productId;

    int availableQuantity;

    InventoryStatus status;

    LocalDateTime replenishmentAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}


