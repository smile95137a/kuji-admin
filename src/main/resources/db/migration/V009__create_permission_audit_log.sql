CREATE TABLE IF NOT EXISTS permission_audit_log (
    id VARCHAR(36) PRIMARY KEY,
    operator_id VARCHAR(36) NOT NULL,
    target_role_id VARCHAR(36) NOT NULL,
    action VARCHAR(50) NOT NULL,
    before_snapshot TEXT,
    after_snapshot TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_role (target_role_id),
    INDEX idx_audit_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='權限變更審計日誌';
