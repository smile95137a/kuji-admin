-- ============================================
-- Order 訂單假資料生成 SQL（簡化版）
-- 不依賴現有資料，可直接執行
-- ============================================

-- 設定變數（MySQL 8.0+）
SET @user_id_1 = UUID();
SET @user_id_2 = UUID();
SET @store_id_1 = UUID();
SET @admin_id = UUID();

-- ============================================
-- 1. 先創建測試用的 User（如果不存在）
-- ============================================
INSERT IGNORE INTO user (
    id, email, password, nickname, status, 
    provider, gold_coins, bonus_coins, 
    email_verified, created_at, updated_at
) VALUES 
(@user_id_1, 'test_user1@example.com', '$2a$10$abcdefg', '測試用戶1', 'ACTIVE', 
 'LOCAL', 1000, 500, 1, NOW(), NOW()),
(@user_id_2, 'test_user2@example.com', '$2a$10$abcdefg', '測試用戶2', 'ACTIVE', 
 'LOCAL', 2000, 1000, 1, NOW(), NOW());

-- ============================================
-- 2. 創建測試用的 Store（如果不存在）
-- ============================================
INSERT IGNORE INTO store (
    id, store_name, store_description, contact_name, 
    contact_email, contact_phone, address, 
    status, created_at, updated_at
) VALUES 
(@store_id_1, '測試抽獎店', '這是測試用的抽獎店家', '店長王大明',
 'store@example.com', '02-12345678', '台北市信義區信義路五段7號',
 'ACTIVE', NOW(), NOW());

-- ============================================
-- 3. 插入 Order 訂單資料（10 筆）
-- ============================================

-- 訂單 1：待處理（店到店）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0001'),
    @user_id_1, @store_id_1,
    3, 'STORE_PICKUP', 'PENDING', 'PAID',
    '王小明', '0912345678', NULL,
    '7-11 信義門市', '7-ELEVEN 信義門市', '台北市信義區信義路五段7號',
    DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)
);

-- 訂單 2：準備出貨中（宅配）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0002'),
    @user_id_2, @store_id_1,
    5, 'HOME_DELIVERY', 'PREPARING', 'PAID',
    '李美華', '0923456789', '新北市板橋區中山路一段123號4樓',
    DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 HOUR)
);

-- 訂單 3：已出貨（宅配）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    tracking_no,
    created_at, updated_at, shipped_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0003'),
    @user_id_1, @store_id_1,
    2, 'HOME_DELIVERY', 'SHIPPED', 'PAID',
    '張大衛', '0934567890', '台中市西屯區台灣大道三段99號',
    'TW123456789',
    DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)
);

-- 訂單 4：已完成（店到店）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone,
    store_code, store_name, store_address, tracking_no,
    created_at, updated_at, shipped_at, completed_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0004'),
    @user_id_2, @store_id_1,
    4, 'STORE_PICKUP', 'COMPLETED', 'PAID',
    '陳小芬', '0945678901',
    '全家 南港門市', 'FamilyMart 南港門市', '台北市南港區南港路一段287號', 'FMT987654321',
    DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 
    DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)
);

-- 訂單 5：已取消
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    remark,
    created_at, updated_at, cancelled_at, cancelled_by, cancel_reason
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0005'),
    @user_id_1, @store_id_1,
    1, 'HOME_DELIVERY', 'CANCELLED', 'REFUNDED',
    '林志明', '0956789012', '高雄市左營區博愛二路777號',
    '客戶要求取消',
    DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 
    DATE_SUB(NOW(), INTERVAL 4 DAY), @admin_id, '客戶改變心意'
);

-- 訂單 6：待處理（多項商品）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address, remark,
    created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0006'),
    @user_id_2, @store_id_1,
    8, 'HOME_DELIVERY', 'PENDING', 'PAID',
    '黃小玉', '0967890123', '桃園市中壢區中山路321號12樓', '重複中獎，非常幸運！',
    DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR)
);

-- 訂單 7：準備出貨中（店到店）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone,
    store_code, store_name, store_address, remark,
    created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0007'),
    @user_id_1, @store_id_1,
    6, 'STORE_PICKUP', 'PREPARING', 'PAID',
    '吳佳琪', '0978901234',
    '萊爾富 師大門市', 'Hi-Life 師大門市', '台北市大安區師大路80號', '請在3天內取貨',
    DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 6 HOUR)
);

-- 訂單 8：已出貨（VIP客戶）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    tracking_no, remark,
    created_at, updated_at, shipped_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0008'),
    @user_id_2, @store_id_1,
    10, 'HOME_DELIVERY', 'SHIPPED', 'PAID',
    '劉建宏', '0989012345', '新竹市東區光復路二段101號',
    'TW555666777', 'VIP 客戶，優先處理',
    DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
);

-- 訂單 9：已完成
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    tracking_no, remark,
    created_at, updated_at, shipped_at, completed_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0009'),
    @user_id_1, @store_id_1,
    3, 'HOME_DELIVERY', 'COMPLETED', 'PAID',
    '許雅婷', '0990123456', '台南市東區中華東路三段88號',
    'TW888999000', '商品完好，客戶滿意',
    DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), 
    DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)
);

-- 訂單 10：待處理（急件）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone,
    store_code, store_name, store_address, remark,
    created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), '0010'),
    @user_id_2, @store_id_1,
    2, 'STORE_PICKUP', 'PENDING', 'PAID',
    '鄭文凱', '0901234567',
    'OK 便利商店 內湖門市', 'OK Mart 內湖門市', '台北市內湖區成功路四段188號', 
    '急件！請優先處理',
    DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)
);

-- ============================================
-- 查詢驗證
-- ============================================

-- 查看所有訂單
SELECT 
    o.order_number,
    o.recipient_name,
    o.status,
    o.shipping_method,
    o.total_items,
    o.payment_status,
    DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') as created_time
FROM `order` o
ORDER BY o.created_at DESC
LIMIT 20;

-- 統計訂單狀態分佈
SELECT 
    status,
    COUNT(*) as count,
    CONCAT(ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM `order`), 1), '%') as percentage
FROM `order`
GROUP BY status
ORDER BY count DESC;

-- ============================================
-- 完成！
-- ============================================
-- ✅ 已創建 10 筆訂單資料
-- ✅ 已創建 2 個測試用戶
-- ✅ 已創建 1 個測試店家
-- 
-- 狀態分佈：
-- - PENDING (待處理): 3 筆
-- - PREPARING (準備出貨中): 2 筆
-- - SHIPPED (已出貨): 2 筆
-- - COMPLETED (已完成): 2 筆
-- - CANCELLED (已取消): 1 筆
-- ============================================
