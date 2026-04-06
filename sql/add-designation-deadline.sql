-- Migration: 新增 designation_deadline 欄位到 lottery_session 表
-- 用途：SCRATCH_PLAYER 模式，開套者有 10 分鐘指定大獎，逾時自動釋放場次
-- 執行時間: 2026-04-06

ALTER TABLE `lottery_session`
  ADD COLUMN `designation_deadline` DATETIME NULL
  COMMENT '指定大獎截止時間（SCRATCH_PLAYER 模式，開套者有 10 分鐘）'
  AFTER `player_designated_numbers`;
