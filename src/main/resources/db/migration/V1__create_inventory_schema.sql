-- Inventory Alert Management — Phase 2 schema
-- MySQL 8.0+
-- Conventions: snake_case tables/columns, BIGINT PKs, ENUM as VARCHAR, UTC timestamps via DATETIME(6)

CREATE TABLE users (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    full_name       VARCHAR(120) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    enabled         TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'MANAGER', 'VIEWER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id                   BIGINT         NOT NULL AUTO_INCREMENT,
    sku                  VARCHAR(64)    NOT NULL,
    name                 VARCHAR(200)   NOT NULL,
    description          VARCHAR(2000)  NULL,
    price                DECIMAL(19, 2) NOT NULL,
    available_quantity   INT            NOT NULL,
    minimum_quantity     INT            NOT NULL,
    active               TINYINT(1)     NOT NULL DEFAULT 1,
    created_at           DATETIME(6)    NOT NULL,
    updated_at           DATETIME(6)    NOT NULL,
    version              BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_price CHECK (price >= 0),
    CONSTRAINT ck_products_available_qty CHECK (available_quantity >= 0),
    CONSTRAINT ck_products_minimum_qty CHECK (minimum_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_products_active ON products (active);
CREATE INDEX idx_products_name ON products (name);

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

-- Supports product ledger pagination ordered by time
CREATE INDEX idx_inv_txn_product_created
    ON inventory_transactions (product_id, created_at DESC);

CREATE TABLE inventory_alerts (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    product_id   BIGINT       NOT NULL,
    alert_type   VARCHAR(30)  NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    message      VARCHAR(1000) NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    resolved_at  DATETIME(6)  NULL,
    CONSTRAINT pk_inventory_alerts PRIMARY KEY (id),
    CONSTRAINT fk_inv_alert_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT ck_inv_alert_type CHECK (alert_type IN ('LOW_STOCK')),
    CONSTRAINT ck_inv_alert_status CHECK (status IN ('PENDING', 'SENT', 'RESOLVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Scheduler / ops: open alerts by status; uniqueness of open alert handled in service for Phase 7
CREATE INDEX idx_inv_alerts_status ON inventory_alerts (status);
CREATE INDEX idx_inv_alerts_product_status ON inventory_alerts (product_id, status);
