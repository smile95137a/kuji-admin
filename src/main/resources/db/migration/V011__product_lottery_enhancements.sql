-- =========================================================
-- V011: Product Lottery Enhancements
-- Applies to: lottery, lottery_prize tables
-- =========================================================

-- 5.1  New columns on lottery
ALTER TABLE lottery
    ADD COLUMN source_lottery_id    VARCHAR(36)  NULL COMMENT '複製來源商品ID'          AFTER remark,
    ADD COLUMN configured_at        DATETIME     NULL COMMENT '進入已配置狀態時間'        AFTER source_lottery_id,
    ADD COLUMN drawable_at          DATETIME     NULL COMMENT '進入可抽狀態時間'          AFTER configured_at,
    ADD COLUMN remaining_draws      INT          NULL COMMENT '剩餘抽獎次數'              AFTER total_draws,
    ADD COLUMN discount_trigger_level VARCHAR(20) NULL COMMENT '觸發降價的獎品等級,例如A,B' AFTER discounted_price,
    ADD COLUMN last_prize_mode      VARCHAR(20)  NULL COMMENT 'LAST_DRAW 或 POOL_IN'    AFTER free_draw_enabled;

-- 5.2  New column on lottery_prize
ALTER TABLE lottery_prize
    ADD COLUMN recycle_bonus BIGINT NOT NULL DEFAULT 0 COMMENT '回收獎勵金額; 0=不可回收' AFTER point_value;

-- 5.3  Indexes
ALTER TABLE lottery ADD INDEX idx_lottery_scheduled_at (scheduled_at);
ALTER TABLE lottery ADD INDEX idx_lottery_start_time   (start_time);
ALTER TABLE lottery ADD INDEX idx_lottery_source_id    (source_lottery_id);
ALTER TABLE lottery_prize ADD INDEX idx_prize_lottery_level (lottery_id, level);
