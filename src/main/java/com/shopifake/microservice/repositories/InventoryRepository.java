package com.shopifake.microservice.repositories;

import com.shopifake.microservice.entities.InventoryItem;
import com.shopifake.microservice.entities.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence layer for inventory items.
 */
public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

    Optional<InventoryItem> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);

    List<InventoryItem> findByStatus(InventoryStatus status);
}


