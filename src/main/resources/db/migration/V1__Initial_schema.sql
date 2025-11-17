-- Inventory schema

CREATE TABLE inventory_items (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL UNIQUE,
    available_quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    replenishment_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_status ON inventory_items(status);
CREATE INDEX idx_inventory_product ON inventory_items(product_id);