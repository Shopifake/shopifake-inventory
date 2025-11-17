package com.shopifake.microservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopifake.microservice.dtos.AdjustInventoryRequest;
import com.shopifake.microservice.dtos.CreateInventoryRequest;
import com.shopifake.microservice.dtos.InventoryResponse;
import com.shopifake.microservice.entities.InventoryStatus;
import com.shopifake.microservice.services.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    private InventoryResponse sampleResponse() {
        return InventoryResponse.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .availableQuantity(10)
                .status(InventoryStatus.IN_STOCK)
                .replenishmentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/inventory creates inventory")
    void shouldCreateInventory() throws Exception {
        InventoryResponse response = sampleResponse();
        when(inventoryService.createInventory(any(CreateInventoryRequest.class))).thenReturn(response);

        CreateInventoryRequest request = CreateInventoryRequest.builder()
                .productId(response.getProductId())
                .initialQuantity(5)
                .build();

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(response.getProductId().toString()))
                .andExpect(jsonPath("$.availableQuantity").value(10));
    }

    @Test
    @DisplayName("GET /api/inventory/{productId} returns inventory")
    void shouldGetInventory() throws Exception {
        InventoryResponse response = sampleResponse();
        when(inventoryService.getInventory(response.getProductId())).thenReturn(response);

        mockMvc.perform(get("/api/inventory/{productId}", response.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_STOCK"));
    }

    @Test
    @DisplayName("GET /api/inventory lists by status")
    void shouldListInventory() throws Exception {
        InventoryResponse response = sampleResponse();
        when(inventoryService.listInventory("IN_STOCK")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/inventory").param("status", "IN_STOCK"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].status").value("IN_STOCK"));
    }

    @Test
    @DisplayName("PATCH /api/inventory/{productId}/adjust updates quantity")
    void shouldAdjustInventory() throws Exception {
        InventoryResponse response = sampleResponse();
        when(inventoryService.adjustInventory(eq(response.getProductId()), any(AdjustInventoryRequest.class)))
                .thenReturn(response);

        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .quantityDelta(3)
                .reason("Manual adjustment")
                .build();

        mockMvc.perform(patch("/api/inventory/{productId}/adjust", response.getProductId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(10));
    }

    @Test
    @DisplayName("DELETE /api/inventory/{productId} removes record")
    void shouldDeleteInventory() throws Exception {
        UUID productId = UUID.randomUUID();
        doNothing().when(inventoryService).deleteInventory(productId);

        mockMvc.perform(delete("/api/inventory/{productId}", productId))
                .andExpect(status().isNoContent());
    }
}


