CREATE TABLE IF NOT EXISTS recharge_order (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    plan_id VARCHAR(36),
    gold_amount BIGINT NOT NULL DEFAULT 0,
    bonus_amount BIGINT NOT NULL DEFAULT 0,
    price_twd BIGINT NOT NULL DEFAULT 0,
    status ENUM('PENDING','SUCCESS','FAILED','EXPIRED') NOT NULL DEFAULT 'PENDING',
    gateway_provider VARCHAR(50),
    gateway_order_id VARCHAR(100),
    gateway_raw_resp TEXT,
    paid_at DATETIME,
    expired_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_recharge_order_user_id (user_id),
    INDEX idx_recharge_order_status (status),
    UNIQUE INDEX uq_gateway_order (gateway_provider, gateway_order_id)
);
