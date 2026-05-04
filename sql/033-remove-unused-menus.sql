-- 033: 移除不需要的後台選單
-- 賞品盒管理已移至會員管理，錢包交易記錄已無需獨立選單
-- 執行日期：2026-05-04

-- 先刪除 role_menu 關聯（避免外鍵衝突）
DELETE FROM role_menu
WHERE menu_id IN (
    SELECT id FROM menu
    WHERE code IN ('PRIZE_BOX_MANAGEMENT', 'WALLET_TRANSACTION')
       OR name IN ('賞品盒管理', '錢包交易記錄')
);

-- 刪除選單本身
DELETE FROM menu
WHERE code IN ('PRIZE_BOX_MANAGEMENT', 'WALLET_TRANSACTION')
   OR name IN ('賞品盒管理', '錢包交易記錄');
