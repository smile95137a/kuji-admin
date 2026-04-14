-- ============================================================
-- 014-store-management migration
-- ============================================================

-- T001: Add missing columns to store table
ALTER TABLE `store`
    ADD COLUMN IF NOT EXISTS `created_by` VARCHAR(36) NULL COMMENT '建立者管理員 ID' AFTER `updated_by`;

-- T002: Create indexes if not exists
CREATE INDEX IF NOT EXISTS `idx_store_status`   ON `store` (`status`);
CREATE INDEX IF NOT EXISTS `idx_store_owner_id` ON `store` (`owner_id`);

-- Migrate existing status values: ACTIVE → ENABLED, INACTIVE → DISABLED
UPDATE `store` SET `status` = 'ENABLED'  WHERE `status` = 'ACTIVE';
UPDATE `store` SET `status` = 'DISABLED' WHERE `status` = 'INACTIVE';
