-- V4: inventory alerts
CREATE TABLE inventory_alerts (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    product_id   BIGINT        NOT NULL,
    alert_type   VARCHAR(30)   NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    message      VARCHAR(1000) NOT NULL,
    created_at   DATETIME(6)   NOT NULL,
    resolved_at  DATETIME(6)   NULL,
    CONSTRAINT pk_inventory_alerts PRIMARY KEY (id),
    CONSTRAINT fk_inv_alert_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT ck_inv_alert_type CHECK (alert_type IN ('LOW_STOCK')),
    CONSTRAINT ck_inv_alert_status CHECK (status IN ('PENDING', 'SENT', 'RESOLVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_inv_alerts_status ON inventory_alerts (status);
CREATE INDEX idx_inv_alerts_product_status ON inventory_alerts (product_id, status);
