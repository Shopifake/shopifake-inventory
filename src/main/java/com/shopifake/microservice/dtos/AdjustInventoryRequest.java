package com.shopifake.microservice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload to adjust inventory levels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustInventoryRequest {

    @NotNull(message = "quantityDelta is required")
    private Integer quantityDelta;

    @NotBlank(message = "reason is required")
    private String reason;
}


