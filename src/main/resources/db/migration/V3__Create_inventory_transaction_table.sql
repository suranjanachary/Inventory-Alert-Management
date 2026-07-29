-- V3: inventory transactions ledger
CREATE TABLE inventory_transactions (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    product_id       BIGINT       NOT NULL,
    transaction_type VARCHAR(20)  NOT NULL,
    quantity         INT          NOT NULL,
    quantity_before  INT          NOT NULL,
    quantity_after   INT          NOT NULL,
    remarks          VARCHAR(500) NULL,
    created_at       DATETIME(6)  NOT NULL,
    CONSTRAINT pk_inventory_transactions PRIMARY KEY (id),
    CONSTRAINT fk_inv_txn_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT ck_inv_txn_type CHECK (transaction_type IN ('PURCHASE', 'SALE')),
    CONSTRAINT ck_inv_txn_quantity CHECK (quantity > 0),
    CONSTRAINT ck_inv_txn_qty_before CHECK (quantity_before >= 0),
    CONSTRAINT ck_inv_txn_qty_after CHECK (quantity_after >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_inv_txn_product_created
    ON inventory_transactions (product_id, created_at DESC);
