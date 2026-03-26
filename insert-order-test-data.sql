-- ============================================
-- Order 訂單假資料生成 SQL
-- 生成日期：2026-01-23
-- ============================================

-- 先查詢現有資料以確保關聯正確
-- SELECT id, email FROM user LIMIT 5;
-- SELECT id, store_name FROM store LIMIT 5;
-- SELECT id, title FROM lottery LIMIT 5;
-- SELECT id, prize_name FROM lottery_prize LIMIT 10;

-- ============================================
-- 插入 Order 主表資料（10 筆訂單）
-- ============================================

-- 訂單 1：待處理（店到店取貨）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1),
    (SELECT id FROM store LIMIT 1),
    3, 
    'STORE_PICKUP', 
    'PENDING', 
    'PAID',
    '王小明',
    '0912345678',
    NULL,
    '7-11 信義門市',
    '7-ELEVEN 信義門市',
    '台北市信義區信義路五段7號',
    NULL,
    '請盡快處理',
    DATE_SUB(NOW(), INTERVAL 2 HOUR),
    DATE_SUB(NOW(), INTERVAL 2 HOUR)
);

-- 訂單 2：準備出貨中（宅配）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1 OFFSET 1),
    (SELECT id FROM store LIMIT 1),
    5, 
    'HOME_DELIVERY', 
    'PREPARING', 
    'PAID',
    '李美華',
    '0923456789',
    '新北市板橋區中山路一段123號4樓',
    NULL,
    NULL,
    NULL,
    NULL,
    '請小心包裝',
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_SUB(NOW(), INTERVAL 1 HOUR)
);

-- 訂單 3：已出貨（宅配）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at, shipped_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1),
    (SELECT id FROM store LIMIT 1),
    2, 
    'HOME_DELIVERY', 
    'SHIPPED', 
    'PAID',
    '張大衛',
    '0934567890',
    '台中市西屯區台灣大道三段99號',
    NULL,
    NULL,
    NULL,
    CONCAT('TW', LPAD(FLOOR(RAND() * 1000000000), 9, '0')),
    NULL,
    DATE_SUB(NOW(), INTERVAL 3 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY)
);

-- 訂單 4：已完成（店到店取貨）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at, shipped_at, completed_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1 OFFSET 1),
    (SELECT id FROM store LIMIT 1),
    4, 
    'STORE_PICKUP', 
    'COMPLETED', 
    'PAID',
    '陳小芬',
    '0945678901',
    NULL,
    '全家 南港門市',
    'FamilyMart 南港門市',
    '台北市南港區南港路一段287號',
    CONCAT('FMT', LPAD(FLOOR(RAND() * 1000000000), 9, '0')),
    '商品狀況良好',
    DATE_SUB(NOW(), INTERVAL 7 DAY),
    DATE_SUB(NOW(), INTERVAL 5 DAY),
    DATE_SUB(NOW(), INTERVAL 6 DAY),
    DATE_SUB(NOW(), INTERVAL 5 DAY)
);

-- 訂單 5：已取消
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at, cancelled_at, cancelled_by, cancel_reason
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1),
    (SELECT id FROM store LIMIT 1),
    1, 
    'HOME_DELIVERY', 
    'CANCELLED', 
    'REFUNDED',
    '林志明',
    '0956789012',
    '高雄市左營區博愛二路777號',
    NULL,
    NULL,
    NULL,
    NULL,
    '客戶要求取消',
    DATE_SUB(NOW(), INTERVAL 5 DAY),
    DATE_SUB(NOW(), INTERVAL 4 DAY),
    DATE_SUB(NOW(), INTERVAL 4 DAY),
    (SELECT id FROM admin_user WHERE email = 'admin@kuji.com'),
    '客戶改變心意'
);

-- 訂單 6：待處理（宅配 - 多項商品）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1 OFFSET 1),
    (SELECT id FROM store LIMIT 1),
    8, 
    'HOME_DELIVERY', 
    'PENDING', 
    'PAID',
    '黃小玉',
    '0967890123',
    '桃園市中壢區中山路321號12樓',
    NULL,
    NULL,
    NULL,
    NULL,
    '重複中獎，非常幸運！',
    DATE_SUB(NOW(), INTERVAL 3 HOUR),
    DATE_SUB(NOW(), INTERVAL 3 HOUR)
);

-- 訂單 7：準備出貨中（店到店）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1),
    (SELECT id FROM store LIMIT 1),
    6, 
    'STORE_PICKUP', 
    'PREPARING', 
    'PAID',
    '吳佳琪',
    '0978901234',
    NULL,
    '萊爾富 師大門市',
    'Hi-Life 師大門市',
    '台北市大安區師大路80號',
    NULL,
    '請在3天內取貨',
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_SUB(NOW(), INTERVAL 6 HOUR)
);

-- 訂單 8：已出貨（宅配 - VIP 客戶）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at, shipped_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1 OFFSET 1),
    (SELECT id FROM store LIMIT 1),
    10, 
    'HOME_DELIVERY', 
    'SHIPPED', 
    'PAID',
    '劉建宏',
    '0989012345',
    '新竹市東區光復路二段101號',
    NULL,
    NULL,
    NULL,
    CONCAT('TW', LPAD(FLOOR(RAND() * 1000000000), 9, '0')),
    'VIP 客戶，優先處理',
    DATE_SUB(NOW(), INTERVAL 4 DAY),
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_SUB(NOW(), INTERVAL 2 DAY)
);

-- 訂單 9：已完成（宅配）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at, shipped_at, completed_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1),
    (SELECT id FROM store LIMIT 1),
    3, 
    'HOME_DELIVERY', 
    'COMPLETED', 
    'PAID',
    '許雅婷',
    '0990123456',
    '台南市東區中華東路三段88號',
    NULL,
    NULL,
    NULL,
    CONCAT('TW', LPAD(FLOOR(RAND() * 1000000000), 9, '0')),
    '商品完好，客戶滿意',
    DATE_SUB(NOW(), INTERVAL 10 DAY),
    DATE_SUB(NOW(), INTERVAL 7 DAY),
    DATE_SUB(NOW(), INTERVAL 8 DAY),
    DATE_SUB(NOW(), INTERVAL 7 DAY)
);

-- 訂單 10：待處理（店到店 - 急件）
INSERT INTO `order` (
    id, order_number, user_id, store_id, 
    total_items, shipping_method, status, payment_status,
    recipient_name, recipient_phone, recipient_address,
    store_code, store_name, store_address,
    tracking_no, remark, created_at, updated_at
) VALUES (
    UUID(), 
    CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(FLOOR(RAND() * 10000), 4, '0')),
    (SELECT id FROM user LIMIT 1 OFFSET 1),
    (SELECT id FROM store LIMIT 1),
    2, 
    'STORE_PICKUP', 
    'PENDING', 
    'PAID',
    '鄭文凱',
    '0901234567',
    NULL,
    'OK 便利商店 內湖門市',
    'OK Mart 內湖門市',
    '台北市內湖區成功路四段188號',
    NULL,
    '急件！請優先處理',
    DATE_SUB(NOW(), INTERVAL 1 HOUR),
    DATE_SUB(NOW(), INTERVAL 1 HOUR)
);

-- ============================================
-- 插入 OrderItem 訂單項目資料
-- ============================================

-- 為每個訂單創建對應的訂單項目
-- 這裡示範前 3 個訂單的項目

-- 訂單 1 的項目（3 項）
INSERT INTO order_item (
    id, order_id, prize_box_id, lottery_id, lottery_title, lottery_image_url,
    prize_id, prize_name, prize_grade, prize_image, prize_image_url, prize_level,
    created_at
)
SELECT 
    UUID() as id,
    (SELECT id FROM `order` WHERE recipient_name = '王小明' LIMIT 1) as order_id,
    pb.id as prize_box_id,
    l.id as lottery_id,
    l.title as lottery_title,
    l.image_url as lottery_image_url,
    lp.id as prize_id,
    lp.prize_name,
    lp.grade as prize_grade,
    lp.image as prize_image,
    lp.image_url as prize_image_url,
    lp.prize_level,
    NOW() as created_at
FROM prize_box pb
JOIN lottery l ON pb.lottery_id = l.id
JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE pb.is_drawn = 1
LIMIT 3;

-- 訂單 2 的項目（5 項）
INSERT INTO order_item (
    id, order_id, prize_box_id, lottery_id, lottery_title, lottery_image_url,
    prize_id, prize_name, prize_grade, prize_image, prize_image_url, prize_level,
    created_at
)
SELECT 
    UUID() as id,
    (SELECT id FROM `order` WHERE recipient_name = '李美華' LIMIT 1) as order_id,
    pb.id as prize_box_id,
    l.id as lottery_id,
    l.title as lottery_title,
    l.image_url as lottery_image_url,
    lp.id as prize_id,
    lp.prize_name,
    lp.grade as prize_grade,
    lp.image as prize_image,
    lp.image_url as prize_image_url,
    lp.prize_level,
    NOW() as created_at
FROM prize_box pb
JOIN lottery l ON pb.lottery_id = l.id
JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE pb.is_drawn = 1
LIMIT 5;

-- 訂單 3 的項目（2 項）
INSERT INTO order_item (
    id, order_id, prize_box_id, lottery_id, lottery_title, lottery_image_url,
    prize_id, prize_name, prize_grade, prize_image, prize_image_url, prize_level,
    created_at
)
SELECT 
    UUID() as id,
    (SELECT id FROM `order` WHERE recipient_name = '張大衛' LIMIT 1) as order_id,
    pb.id as prize_box_id,
    l.id as lottery_id,
    l.title as lottery_title,
    l.image_url as lottery_image_url,
    lp.id as prize_id,
    lp.prize_name,
    lp.grade as prize_grade,
    lp.image as prize_image,
    lp.image_url as prize_image_url,
    lp.prize_level,
    NOW() as created_at
FROM prize_box pb
JOIN lottery l ON pb.lottery_id = l.id
JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE pb.is_drawn = 1
LIMIT 2;

-- ============================================
-- 插入 OrderStatusLog 訂單狀態變更記錄
-- ============================================

-- 訂單 1：待處理
INSERT INTO order_status_log (
    id, order_id, from_status, to_status, 
    changed_by, change_reason, created_at
) VALUES (
    UUID(),
    (SELECT id FROM `order` WHERE recipient_name = '王小明' LIMIT 1),
    NULL,
    'PENDING',
    (SELECT id FROM user WHERE email LIKE '%@%' LIMIT 1),
    '訂單建立',
    DATE_SUB(NOW(), INTERVAL 2 HOUR)
);

-- 訂單 2：待處理 → 準備出貨中
INSERT INTO order_status_log (
    id, order_id, from_status, to_status, 
    changed_by, change_reason, created_at
) VALUES 
(
    UUID(),
    (SELECT id FROM `order` WHERE recipient_name = '李美華' LIMIT 1),
    NULL,
    'PENDING',
    (SELECT id FROM user LIMIT 1 OFFSET 1),
    '訂單建立',
    DATE_SUB(NOW(), INTERVAL 1 DAY)
),
(
    UUID(),
    (SELECT id FROM `order` WHERE recipient_name = '李美華' LIMIT 1),
    'PENDING',
    'PREPARING',
    (SELECT id FROM admin_user WHERE email = 'admin@kuji.com'),
    '開始準備出貨',
    DATE_SUB(NOW(), INTERVAL 1 HOUR)
);

-- 訂單 3：待處理 → 準備出貨中 → 已出貨
INSERT INTO order_status_log (
    id, order_id, from_status, to_status, 
    changed_by, change_reason, created_at
) VALUES 
(
    UUID(),
    (SELECT id FROM `order` WHERE recipient_name = '張大衛' LIMIT 1),
    NULL,
    'PENDING',
    (SELECT id FROM user LIMIT 1),
    '訂單建立',
    DATE_SUB(NOW(), INTERVAL 3 DAY)
),
(
    UUID(),
    (SELECT id FROM `order` WHERE recipient_name = '張大衛' LIMIT 1),
    'PENDING',
    'PREPARING',
    (SELECT id FROM admin_user WHERE email = 'admin@kuji.com'),
    '開始準備出貨',
    DATE_SUB(NOW(), INTERVAL 2 DAY)
),
(
    UUID(),
    (SELECT id FROM `order` WHERE recipient_name = '張大衛' LIMIT 1),
    'PREPARING',
    'SHIPPED',
    (SELECT id FROM admin_user WHERE email = 'admin@kuji.com'),
    '已交付物流',
    DATE_SUB(NOW(), INTERVAL 1 DAY)
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
    u.nickname as user_nickname,
    s.store_name,
    o.created_at
FROM `order` o
LEFT JOIN user u ON o.user_id = u.id
LEFT JOIN store s ON o.store_id = s.id
ORDER BY o.created_at DESC;

-- 查看訂單項目數量
SELECT 
    o.order_number,
    o.recipient_name,
    o.total_items as expected_items,
    COUNT(oi.id) as actual_items
FROM `order` o
LEFT JOIN order_item oi ON o.id = oi.order_id
GROUP BY o.id, o.order_number, o.recipient_name, o.total_items
ORDER BY o.created_at DESC;

-- 查看訂單狀態變更記錄
SELECT 
    o.order_number,
    osl.from_status,
    osl.to_status,
    osl.change_reason,
    osl.created_at
FROM order_status_log osl
JOIN `order` o ON osl.order_id = o.id
ORDER BY o.order_number, osl.created_at;

-- ============================================
-- 完成！
-- ============================================
-- 已創建 10 筆訂單資料
-- 狀態分佈：
-- - PENDING (待處理): 3 筆
-- - PREPARING (準備出貨中): 2 筆
-- - SHIPPED (已出貨): 2 筆
-- - COMPLETED (已完成): 2 筆
-- - CANCELLED (已取消): 1 筆
-- ============================================
