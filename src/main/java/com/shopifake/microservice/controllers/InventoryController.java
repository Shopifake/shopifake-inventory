package com.shopifake.microservice.controllers;

import com.shopifake.microservice.dtos.AdjustInventoryRequest;
import com.shopifake.microservice.dtos.CreateInventoryRequest;
import com.shopifake.microservice.dtos.InventoryResponse;
import com.shopifake.microservice.services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Inventory REST endpoints.
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create inventory record for product")
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody final CreateInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createInventory(request));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory by product")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable final UUID productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @GetMapping
    @Operation(summary = "List inventory rows")
    public ResponseEntity<List<InventoryResponse>> listInventory(
            @RequestParam(required = false) final String status) {
        return ResponseEntity.ok(inventoryService.listInventory(status));
    }

    @PatchMapping("/{productId}/adjust")
    @Operation(summary = "Adjust on-hand quantity")
    public ResponseEntity<InventoryResponse> adjustInventory(
            @PathVariable final UUID productId,
            @Valid @RequestBody final AdjustInventoryRequest request) {
        return ResponseEntity.ok(inventoryService.adjustInventory(productId, request));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete inventory record")
    public ResponseEntity<Void> deleteInventory(
            @PathVariable final UUID productId) {
        inventoryService.deleteInventory(productId);
        return ResponseEntity.noContent().build();
    }
}


