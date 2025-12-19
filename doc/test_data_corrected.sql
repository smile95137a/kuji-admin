-- =============================================
-- KUJI 抽獎平台測試資料 (完整版 - 已修正所有欄位對應)
-- 生成時間: 2025-12-18
-- 說明: 此檔案包含所有主要資料表的測試資料，欄位已與 Entity 完全對應
-- =============================================

-- 設定字元編碼
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 一、角色資料 (role)
-- =============================================

INSERT INTO role (id, name, code, description, created_at, updated_at) VALUES
(1, '系統管理員', 'ROLE_ADMIN', '平台最高權限管理者，可管理所有店家與系統設定', NOW(), NOW()),
(2, '店家負責人', 'ROLE_STORE_OWNER', '店家主帳號，可管理自己店家的商品、訂單與報表', NOW(), NOW()),
(3, '店家編輯', 'ROLE_STORE_EDITOR', '店家小編帳號，僅能編輯商品與查看訂單', NOW(), NOW());


-- =============================================
-- 二、選單資料 (menu)
-- =============================================

-- 第一層選單
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible, created_at, updated_at) VALUES
(1, '店家管理', 'STORE_MANAGEMENT', '/admin/stores', NULL, 'store', 1, 1, NOW(), NOW()),
(2, '商品管理', 'LOTTERY_MANAGEMENT', '/admin/lotteries', NULL, 'shopping', 2, 1, NOW(), NOW()),
(3, '訂單管理', 'ORDER_MANAGEMENT', '/admin/orders', NULL, 'receipt', 3, 1, NOW(), NOW()),
(4, '會員管理', 'USER_MANAGEMENT', '/admin/users', NULL, 'people', 4, 1, NOW(), NOW()),
(5, '報表中心', 'REPORT_CENTER', '/admin/reports', NULL, 'chart', 5, 1, NOW(), NOW()),
(6, '權限管理', 'PERMISSION_MANAGEMENT', '/admin/permissions', NULL, 'security', 6, 1, NOW(), NOW());

-- 第二層選單 - 店家管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible, created_at, updated_at) VALUES
(11, '店家列表', 'STORE_LIST', '/admin/stores/list', 1, NULL, 1, 1, NOW(), NOW()),
(12, '新增店家', 'STORE_CREATE', '/admin/stores/create', 1, NULL, 2, 1, NOW(), NOW());

-- 第二層選單 - 商品管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible, created_at, updated_at) VALUES
(21, '商品列表', 'LOTTERY_LIST', '/admin/lotteries/list', 2, NULL, 1, 1, NOW(), NOW()),
(22, '新增商品', 'LOTTERY_CREATE', '/admin/lotteries/create', 2, NULL, 2, 1, NOW(), NOW()),
(23, '獎品管理', 'PRIZE_MANAGEMENT', '/admin/prizes', 2, NULL, 3, 1, NOW(), NOW());

-- 第二層選單 - 訂單管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible, created_at, updated_at) VALUES
(31, '訂單列表', 'ORDER_LIST', '/admin/orders/list', 3, NULL, 1, 1, NOW(), NOW()),
(32, '配送管理', 'SHIPPING_MANAGEMENT', '/admin/shipping', 3, NULL, 2, 1, NOW(), NOW());

-- 第二層選單 - 報表中心
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible, created_at, updated_at) VALUES
(51, '營收報表', 'REVENUE_REPORT', '/admin/reports/revenue', 5, NULL, 1, 1, NOW(), NOW()),
(52, '抽獎統計', 'DRAW_STATISTICS', '/admin/reports/draw-stats', 5, NULL, 2, 1, NOW(), NOW());

-- 第二層選單 - 權限管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible, created_at, updated_at) VALUES
(61, '角色管理', 'ROLE_MANAGEMENT', '/admin/permissions/roles', 6, NULL, 1, 1, NOW(), NOW()),
(62, '選單管理', 'MENU_MANAGEMENT', '/admin/permissions/menus', 6, NULL, 2, 1, NOW(), NOW()),
(63, '帳號管理', 'ACCOUNT_MANAGEMENT', '/admin/permissions/accounts', 6, NULL, 3, 1, NOW(), NOW());


-- =============================================
-- 三、角色選單權限 (role_menu)
-- =============================================

-- Admin 擁有所有權限
INSERT INTO role_menu (role_id, menu_id, can_view, can_edit, can_delete, created_at) 
SELECT 1, id, 1, 1, 1, NOW() FROM menu;

-- StoreOwner 權限 (不含權限管理)
INSERT INTO role_menu (role_id, menu_id, can_view, can_edit, can_delete, created_at) VALUES
(2, 2, 1, 1, 1, NOW()),   -- 商品管理
(2, 21, 1, 1, 1, NOW()),  -- 商品列表
(2, 22, 1, 1, 0, NOW()),  -- 新增商品
(2, 23, 1, 1, 1, NOW()),  -- 獎品管理
(2, 3, 1, 1, 0, NOW()),   -- 訂單管理
(2, 31, 1, 0, 0, NOW()),  -- 訂單列表（僅查看）
(2, 32, 1, 1, 0, NOW()),  -- 配送管理
(2, 5, 1, 0, 0, NOW()),   -- 報表中心
(2, 51, 1, 0, 0, NOW()),  -- 營收報表（僅查看）
(2, 52, 1, 0, 0, NOW());  -- 抽獎統計（僅查看）

-- StoreEditor 權限 (僅商品與訂單查看)
INSERT INTO role_menu (role_id, menu_id, can_view, can_edit, can_delete, created_at) VALUES
(3, 2, 1, 1, 0, NOW()),   -- 商品管理
(3, 21, 1, 1, 0, NOW()),  -- 商品列表
(3, 23, 1, 1, 0, NOW()),  -- 獎品管理
(3, 3, 1, 0, 0, NOW()),   -- 訂單管理
(3, 31, 1, 0, 0, NOW());  -- 訂單列表（僅查看）


-- =============================================
-- 四、管理者帳號 (admin_user)
-- 注意：所有密碼的 BCrypt Hash 為 Test1234
-- =============================================

-- 系統管理員（密碼: admin123）
-- BCrypt Hash: $2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ
INSERT INTO admin_user (id, username, password, email, display_name, phone, status, force_change_password, created_by, created_at, updated_at) VALUES
(1, 'admin@kuji.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'admin@kuji.com', '系統管理員', '0900000000', 'ACTIVE', 0, NULL, NOW(), NOW());

-- 測試店家負責人（密碼: Test1234）
INSERT INTO admin_user (id, username, password, email, display_name, phone, status, force_change_password, created_by, remark, created_at, updated_at) VALUES
(2, 'owner@teststore.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'owner@teststore.com', '測試店家老闆', '0911111111', 'ACTIVE', 0, 1, '測試用店家負責人', NOW(), NOW()),
(3, 'owner2@teststore.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'owner2@teststore.com', '第二間店家老闆', '0922222222', 'ACTIVE', 0, 1, '測試用店家負責人', NOW(), NOW());

-- 測試店家編輯人員（密碼: Test1234）
INSERT INTO admin_user (id, username, password, email, display_name, phone, status, force_change_password, created_by, remark, created_at, updated_at) VALUES
(4, 'editor@teststore.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'editor@teststore.com', '測試店家小編', '0933333333', 'ACTIVE', 0, 1, '測試用店家編輯人員', NOW(), NOW());

-- 待啟用帳號（需首次修改密碼）
INSERT INTO admin_user (id, username, password, email, display_name, phone, status, force_change_password, created_by, remark, created_at, updated_at) VALUES
(5, 'pending@teststore.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'pending@teststore.com', '待啟用帳號', '0944444444', 'PENDING', 1, 1, '測試首次登入流程', NOW(), NOW());


-- =============================================
-- 五、使用者角色關聯 (admin_user_role)
-- =============================================

INSERT INTO admin_user_role (admin_user_id, role_id, created_at) VALUES
(1, 1, NOW()),  -- admin@kuji.com -> Admin
(2, 2, NOW()),  -- owner@teststore.com -> StoreOwner
(3, 2, NOW()),  -- owner2@teststore.com -> StoreOwner
(4, 3, NOW()),  -- editor@teststore.com -> StoreEditor
(5, 2, NOW());  -- pending@teststore.com -> StoreOwner


-- =============================================
-- 六、店家資料 (store)
-- =============================================

INSERT INTO store (id, owner_id, store_name, short_description, long_description, logo_url, cover_image_url, email, phone, address, facebook_url, instagram_url, line_id, business_hours, status, remark, created_at, updated_at, updated_by) VALUES
(1, 2, 'KUJI 測試商店', '最好玩的抽獎商店', '這是一間專門販售各種精美獎品的抽獎商店，歡迎來試手氣！', 'https://via.placeholder.com/200', 'https://via.placeholder.com/1200x400', 'owner@teststore.com', '0911111111', '台北市信義區信義路五段7號', 'https://facebook.com/kujitest', 'https://instagram.com/kujitest', '@kujitest', '每日 10:00~22:00', 'ACTIVE', '測試用店家', NOW(), NOW(), 1),
(2, 3, '動漫周邊專賣店', '動漫迷必逛的抽獎店', '專營日本動漫周邊、公仔、模型等精品，採用一番賞抽獎機制。', 'https://via.placeholder.com/200', 'https://via.placeholder.com/1200x400', 'owner2@teststore.com', '0922222222', '台北市中山區南京東路三段168號', 'https://facebook.com/animestore', 'https://instagram.com/animestore', '@animestore', '每日 11:00~21:00', 'ACTIVE', '測試用店家', NOW(), NOW(), 1);


-- =============================================
-- 七、店家使用者關聯 (store_user)
-- =============================================

INSERT INTO store_user (store_id, admin_user_id, role_type, created_at) VALUES
(1, 2, 'OWNER', NOW()),   -- 測試商店 - owner@teststore.com
(1, 4, 'EDITOR', NOW()),  -- 測試商店 - editor@teststore.com
(2, 3, 'OWNER', NOW());   -- 動漫周邊專賣店 - owner2@teststore.com


-- =============================================
-- 八、前台會員 (user)
-- 注意：User 表欄位為 provider, providerId (非 auth_provider)
-- goldCoins, bonusCoins (非 total_gold_points)
-- =============================================

INSERT INTO `user` (id, email, password, nickname, avatar, provider, provider_id, status, phone_number, gold_coins, bonus_coins, created_at, updated_at) VALUES
(1, 'user1@test.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', '測試會員A', 'https://via.placeholder.com/100', 'EMAIL', NULL, 'ACTIVE', '0955555555', 1000, 500, NOW(), NOW()),
(2, 'user2@test.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', '測試會員B', 'https://via.placeholder.com/100', 'EMAIL', NULL, 'ACTIVE', '0966666666', 2500, 300, NOW(), NOW()),
(3, 'googleuser@gmail.com', NULL, 'Google 測試會員', 'https://via.placeholder.com/100', 'GOOGLE', 'google_oauth_id_12345', 'ACTIVE', NULL, 500, 100, NOW(), NOW());


-- =============================================
-- 九、抽獎活動 (lottery)
-- 注意：欄位為 pricePerDraw (非 single_draw_price)
-- discountedPrice (非 five_draw_price/ten_draw_price)
-- totalDraws, maxDraws (非 total_quantity, remaining_quantity)
-- imageUrl (非 main_image_url)
-- =============================================

INSERT INTO lottery (id, store_id, title, description, category, sub_category, status, price_per_draw, discounted_price, auto_discount_enabled, allow_multi_draw, multi_draw_options, scheduled_at, start_time, end_time, total_draws, max_draws, order_num, weight, image_url, created_by, created_at, updated_at, remark) VALUES
(1, 1, '鬼滅之刃一番賞', '超人氣鬼滅之刃一番賞，多款精美公仔等你來抽！', 'OFFICIAL_ICHIBAN', 'LOTTERY_MODE', 'ON_SHELF', 80, 720, 0, 1, '5,10', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 80, 80, 1, 100, 'https://via.placeholder.com/400x300', 2, NOW(), NOW(), '測試用商品'),
(2, 1, '咒術迴戰刮刮樂', '咒術迴戰限定刮刮樂，每張都有獎！', 'OFFICIAL_ICHIBAN', 'SCRATCH_CARD_MODE', 'ON_SHELF', 60, 540, 0, 1, '5,10', NOW(), NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 100, 100, 2, 90, 'https://via.placeholder.com/400x300', 2, NOW(), NOW(), '測試用商品'),
(3, 2, '初音未來限定賞', '初音未來 15 週年紀念限定賞', 'GACHA', 'LOTTERY_MODE', 'DRAFT', 120, 1080, 1, 1, '5,10', DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 37 DAY), 50, 50, 1, 95, 'https://via.placeholder.com/400x300', 3, NOW(), NOW(), '測試用商品');


-- =============================================
-- 十、獎品資料 (lottery_prize)
-- 注意：欄位為 quantity, remaining (非 total_quantity, remaining_quantity)
-- isLastPrize, isGrandPrize (非 is_last_prize)
-- =============================================

-- 鬼滅之刃一番賞獎品
INSERT INTO lottery_prize (lottery_id, level, name, description, quantity, remaining, image_url, order_num, is_last_prize, is_grand_prize, prize_type, created_at, updated_at) VALUES
(1, 'A', '炭治郎公仔（大）', '約 25cm 高品質公仔', 1, 1, 'https://via.placeholder.com/200', 1, 0, 0, 'FIGURE', NOW(), NOW()),
(1, 'B', '禰豆子公仔（大）', '約 25cm 高品質公仔', 1, 1, 'https://via.placeholder.com/200', 2, 0, 0, 'FIGURE', NOW(), NOW()),
(1, 'C', '善逸公仔', '約 18cm 精緻公仔', 3, 3, 'https://via.placeholder.com/200', 3, 0, 0, 'FIGURE', NOW(), NOW()),
(1, 'D', '伊之助公仔', '約 18cm 精緻公仔', 5, 4, 'https://via.placeholder.com/200', 4, 0, 0, 'FIGURE', NOW(), NOW()),
(1, 'E', '壓克力立牌', '隨機角色壓克力立牌', 20, 18, 'https://via.placeholder.com/200', 5, 0, 0, 'GOODS', NOW(), NOW()),
(1, 'F', '徽章組', '隨機 3 入徽章組', 30, 25, 'https://via.placeholder.com/200', 6, 0, 0, 'GOODS', NOW(), NOW()),
(1, 'G', '貼紙包', '隨機貼紙包', 19, 12, 'https://via.placeholder.com/200', 7, 0, 0, 'GOODS', NOW(), NOW()),
(1, 'LAST_PRIZE', '特別版炭治郎公仔', '最後一抽限定公仔', 1, 1, 'https://via.placeholder.com/200', 8, 1, 0, 'FIGURE', NOW(), NOW());

-- 咒術迴戰刮刮樂獎品
INSERT INTO lottery_prize (lottery_id, level, name, description, quantity, remaining, image_url, order_num, is_last_prize, is_grand_prize, prize_type, created_at, updated_at) VALUES
(2, 'A', '五條悟公仔', '約 20cm 公仔', 2, 2, 'https://via.placeholder.com/200', 1, 0, 1, 'FIGURE', NOW(), NOW()),
(2, 'B', '虎杖悠仁公仔', '約 18cm 公仔', 5, 5, 'https://via.placeholder.com/200', 2, 0, 0, 'FIGURE', NOW(), NOW()),
(2, 'C', '壓克力鑰匙圈', '隨機角色鑰匙圈', 20, 20, 'https://via.placeholder.com/200', 3, 0, 0, 'GOODS', NOW(), NOW()),
(2, 'D', '透明資料夾', '隨機角色資料夾', 33, 33, 'https://via.placeholder.com/200', 4, 0, 0, 'GOODS', NOW(), NOW()),
(2, 'E', '小貼紙', '隨機小貼紙', 40, 40, 'https://via.placeholder.com/200', 5, 0, 0, 'GOODS', NOW(), NOW());


-- =============================================
-- 十一、點數紀錄範例 (point_log)
-- 注意：欄位為 beforeBalance, afterBalance (非 balance_after)
-- referenceId 為 VARCHAR (非 BIGINT)
-- =============================================

INSERT INTO point_log (user_id, point_type, operation_type, amount, before_balance, after_balance, reference_type, reference_id, remark, created_at) VALUES
(1, 'GOLD', 'DEPOSIT', 1000, 0, 1000, 'PAYMENT', '1001', '儲值金點 NT$1000', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'BONUS', 'BONUS_GRANT', 500, 0, 500, 'SYSTEM', NULL, '新會員首儲贈送紅利點', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'GOLD', 'DRAW', -80, 1000, 920, 'LOTTERY_DRAW', '1', '抽獎消費：鬼滅之刃一番賞', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(2, 'GOLD', 'DEPOSIT', 3000, 0, 3000, 'PAYMENT', '1002', '儲值金點 NT$3000', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 'BONUS', 'BONUS_GRANT', 300, 0, 300, 'SYSTEM', NULL, '儲值滿額贈送紅利點', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 'GOLD', 'DRAW', -380, 3000, 2620, 'LOTTERY_DRAW', '1', '抽獎消費：鬼滅之刃一番賞 x5', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 'BONUS', 'DRAW', -120, 300, 180, 'LOTTERY_DRAW', '1', '抽獎消費（紅利折抵）', DATE_SUB(NOW(), INTERVAL 15 DAY));


-- =============================================
-- 十二、抽獎紀錄範例 (lottery_draw_record)
-- 注意：欄位為 costType, costAmount (非 point_type, points_used)
-- selectedNumber (非 prize_number)
-- =============================================

INSERT INTO lottery_draw_record (lottery_id, user_id, prize_id, selected_number, cost_type, cost_amount, status, created_at) VALUES
(1, 1, 6, NULL, 'GOLD', 80, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 2, 5, NULL, 'GOLD', 76, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 2, 6, NULL, 'GOLD', 76, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 2, 6, NULL, 'GOLD', 76, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 2, 7, NULL, 'GOLD', 76, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 2, 7, NULL, 'BONUS', 76, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 15 DAY));


-- =============================================
-- 十三、重設 AUTO_INCREMENT（避免 ID 衝突）
-- =============================================

ALTER TABLE role AUTO_INCREMENT = 10;
ALTER TABLE menu AUTO_INCREMENT = 100;
ALTER TABLE role_menu AUTO_INCREMENT = 1000;
ALTER TABLE admin_user AUTO_INCREMENT = 100;
ALTER TABLE admin_user_role AUTO_INCREMENT = 100;
ALTER TABLE store AUTO_INCREMENT = 10;
ALTER TABLE store_user AUTO_INCREMENT = 100;
ALTER TABLE `user` AUTO_INCREMENT = 100;
ALTER TABLE lottery AUTO_INCREMENT = 100;
ALTER TABLE lottery_prize AUTO_INCREMENT = 1000;
ALTER TABLE point_log AUTO_INCREMENT = 10000;
ALTER TABLE lottery_draw_record AUTO_INCREMENT = 10000;


SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 測試資料插入完成
-- =============================================
-- 
-- 測試帳號資訊:
-- 1. Admin: admin@kuji.com / admin123
-- 2. StoreOwner1: owner@teststore.com / Test1234
-- 3. StoreOwner2: owner2@teststore.com / Test1234
-- 4. StoreEditor: editor@teststore.com / Test1234
-- 5. Pending: pending@teststore.com / Test1234 (需首次修改密碼)
--
-- 測試會員:
-- 1. user1@test.com / Test1234 (金點 1000, 紅利 500)
-- 2. user2@test.com / Test1234 (金點 2500, 紅利 300)
-- 3. googleuser@gmail.com (Google 登入, 金點 500, 紅利 100)
--
-- 測試店家:
-- 1. KUJI 測試商店 (ID: 1, Owner: owner@teststore.com)
-- 2. 動漫周邊專賣店 (ID: 2, Owner: owner2@teststore.com)
--
-- 測試商品:
-- 1. 鬼滅之刃一番賞 (ID: 1, 80 抽, 已上架)
-- 2. 咒術迴戰刮刮樂 (ID: 2, 100 抽, 已上架)
-- 3. 初音未來限定賞 (ID: 3, 50 抽, 草稿)
-- =============================================
