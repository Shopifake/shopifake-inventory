package com.shopifake.microservice.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request payload to bootstrap inventory for a product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequest {

    @NotNull(message = "productId is required")
    private UUID productId;

    @Min(value = 0, message = "initialQuantity cannot be negative")
    private int initialQuantity;
}


