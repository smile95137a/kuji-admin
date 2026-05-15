-- Migration: add business_hours_json column and backfill JSON values
-- 注意：請在非高峰時段執行，並先在測試環境驗證

-- 1) 新增欄位（MySQL 5.7+ 支援 JSON 類型）
ALTER TABLE `store`
  ADD COLUMN `business_hours_json` JSON DEFAULT NULL;

-- 2) 將已經是 JSON 格式的舊值複製到新欄位
-- JSON_VALID() 會檢查字串是否為有效 JSON
UPDATE `store`
SET `business_hours_json` = CAST(`business_hours` AS JSON)
WHERE JSON_VALID(`business_hours`);

-- 3) 檢查尚未被遷移的列（人工檢視自由文字格式並決定處理方式）
SELECT id, business_hours
FROM `store`
WHERE JSON_VALID(`business_hours`) = 0 AND (`business_hours` IS NOT NULL AND TRIM(`business_hours`) != '');

-- 可選：把舊欄位內的 JSON 值清空（僅在確認所有服務已改為使用 business_hours_json 時執行）
-- UPDATE `store` SET `business_hours` = NULL WHERE JSON_VALID(`business_hours`);

-- ROLLBACK（如需回滾）
-- ALTER TABLE `store` DROP COLUMN `business_hours_json`;

-- 注意事項：
-- - 對於非 JSON 的自由文字（例如："每日 10:00~22:00"），請人工或寫批次程式將其轉換為結構化 JSON，或將其保留在 `business_hours` 作為備援。
-- - 若使用 MariaDB 或舊版 MySQL，若不支援 JSON 類型，可改用 LONGTEXT/JSON 字串欄位。
