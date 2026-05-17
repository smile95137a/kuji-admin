-- 讓 store.business_hours 可存結構化營業時間 JSON
-- 原本欄位長度不足，會在更新店家資料時出現：
-- Data truncation: Data too long for column 'business_hours'

ALTER TABLE `store`
    MODIFY COLUMN `business_hours` LONGTEXT NULL COMMENT '營業時間（可存文字或結構化 JSON）';
