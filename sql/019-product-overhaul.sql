-- ============================================================
-- spec 019-product-overhaul DDL
-- Adds: payment_type, free_draw_threshold, delist_strategy
-- Relaxes: multi_draw_options, allow_multi_draw, protection_draws, protection_minutes
-- ============================================================

ALTER TABLE lottery
  ADD COLUMN IF NOT EXISTS payment_type VARCHAR(20) NOT NULL DEFAULT 'GOLD' COMMENT '付款方式',
  ADD COLUMN IF NOT EXISTS free_draw_threshold INT NULL COMMENT '免費抽門檻（刮刮樂）',
  ADD COLUMN IF NOT EXISTS delist_strategy VARCHAR(30) NOT NULL DEFAULT 'ALL_DRAWN' COMMENT '下架策略';

ALTER TABLE lottery
  MODIFY COLUMN multi_draw_options VARCHAR(100) NULL,
  MODIFY COLUMN allow_multi_draw TINYINT(1) NULL DEFAULT 0,
  MODIFY COLUMN protection_draws INT NULL,
  MODIFY COLUMN protection_minutes INT NULL;
