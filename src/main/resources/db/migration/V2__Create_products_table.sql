-- V2: products table
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
