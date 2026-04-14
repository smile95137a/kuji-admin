CREATE TABLE IF NOT EXISTS recharge_order (
    id                  VARCHAR(36)   NOT NULL PRIMARY KEY,
    user_id             VARCHAR(36)   NOT NULL,
    plan_id             VARCHAR(36)   NOT NULL,
    gold_amount         BIGINT        NOT NULL,
    bonus_amount        BIGINT        NOT NULL DEFAULT 0,
    price_twd           DECIMAL(10,2) NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    gateway_provider    VARCHAR(50)   NULL,
    gateway_order_id    VARCHAR(100)  NULL,
    gateway_raw_resp    TEXT          NULL,
    paid_at             DATETIME      NULL,
    expired_at          DATETIME      NOT NULL,
    created_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ro_user_id (user_id),
    INDEX idx_ro_status  (status),
    INDEX idx_ro_expired (expired_at, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
