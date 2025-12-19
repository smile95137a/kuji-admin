-- =============================================
-- KUJI 抽獎平台 測試資料
-- 版本: 2025-01-15
-- =============================================

-- 清除現有測試資料（可選）
-- DELETE FROM admin_operation_log;
-- DELETE FROM lottery_draw_record;
-- DELETE FROM lottery_prize;
-- DELETE FROM lottery_lock;
-- DELETE FROM lottery;
-- DELETE FROM point_log;
-- DELETE FROM `order`;
-- DELETE FROM banner;
-- DELETE FROM store_user;
-- DELETE FROM role_menu;
-- DELETE FROM admin_user_role;
-- DELETE FROM store;
-- DELETE FROM admin_user WHERE id > 1;

-- =============================================
-- 一、角色資料 (role)
-- =============================================

INSERT INTO role (id, name, code, description) VALUES
(1, 'Admin', 'ROLE_ADMIN', '平台最高管理者，管理所有店家、所有權限'),
(2, 'StoreOwner', 'ROLE_STORE_OWNER', '店家主帳號，管理自己店家的商品、訂單、報表'),
(3, 'StoreEditor', 'ROLE_STORE_EDITOR', '店家小編，僅能操作部分功能（商品、訂單）');


-- =============================================
-- 二、選單資料 (menu)
-- =============================================

-- 頂層選單
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible) VALUES
(1, '儀表板', 'DASHBOARD', '/dashboard', NULL, 'dashboard', 1, 1),
(2, '店家管理', 'STORE', '/stores', NULL, 'store', 2, 1),
(3, '帳號管理', 'USER', '/users', NULL, 'user', 3, 1),
(4, '抽獎管理', 'LOTTERY', '/lottery', NULL, 'gift', 4, 1),
(5, '訂單管理', 'ORDER', '/orders', NULL, 'shopping-cart', 5, 1),
(6, '會員管理', 'MEMBER', '/members', NULL, 'users', 6, 1),
(7, '系統設定', 'SYSTEM', '/system', NULL, 'settings', 99, 1);

-- 子選單：店家管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible) VALUES
(21, '店家列表', 'STORE_LIST', '/stores/list', 2, NULL, 1, 1),
(22, '店家詳情', 'STORE_DETAIL', '/stores/:id', 2, NULL, 2, 0);

-- 子選單：帳號管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible) VALUES
(31, '帳號列表', 'USER_LIST', '/users/list', 3, NULL, 1, 1),
(32, '新增帳號', 'USER_CREATE', '/users/create', 3, NULL, 2, 1);

-- 子選單：抽獎管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible) VALUES
(41, '抽獎列表', 'LOTTERY_LIST', '/lottery/list', 4, NULL, 1, 1),
(42, '新增抽獎', 'LOTTERY_CREATE', '/lottery/create', 4, NULL, 2, 1),
(43, '獎池管理', 'LOTTERY_PRIZE', '/lottery/:id/prizes', 4, NULL, 3, 0);

-- 子選單：訂單管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible) VALUES
(51, '訂單列表', 'ORDER_LIST', '/orders/list', 5, NULL, 1, 1),
(52, '訂單詳情', 'ORDER_DETAIL', '/orders/:id', 5, NULL, 2, 0);

-- 子選單：會員管理
INSERT INTO menu (id, name, code, path, parent_id, icon, order_num, is_visible) VALUES
(61, '會員列表', 'MEMBER_LIST', '/members/list', 6, NULL, 1, 1),
(62, '點數紀錄', 'MEMBER_POINTS', '/members/points', 6, NULL, 2, 1);


-- =============================================
-- 三、角色選單權限 (role_menu)
-- =============================================

-- Admin 擁有所有權限
INSERT INTO role_menu (role_id, menu_id, can_view, can_edit, can_delete) VALUES
(1, 1, 1, 1, 1),  -- 儀表板
(1, 2, 1, 1, 1),  -- 店家管理
(1, 3, 1, 1, 1),  -- 帳號管理
(1, 4, 1, 1, 1),  -- 抽獎管理
(1, 5, 1, 1, 1),  -- 訂單管理
(1, 6, 1, 1, 1),  -- 會員管理
(1, 7, 1, 1, 1),  -- 系統設定
(1, 21, 1, 1, 1), -- 店家列表
(1, 22, 1, 1, 1), -- 店家詳情
(1, 31, 1, 1, 1), -- 帳號列表
(1, 32, 1, 1, 1), -- 新增帳號
(1, 41, 1, 1, 1), -- 抽獎列表
(1, 42, 1, 1, 1), -- 新增抽獎
(1, 43, 1, 1, 1), -- 獎池管理
(1, 51, 1, 1, 1), -- 訂單列表
(1, 52, 1, 1, 1), -- 訂單詳情
(1, 61, 1, 1, 1), -- 會員列表
(1, 62, 1, 1, 1); -- 點數紀錄

-- StoreOwner 權限（管理自己店家）
INSERT INTO role_menu (role_id, menu_id, can_view, can_edit, can_delete) VALUES
(2, 1, 1, 0, 0),  -- 儀表板（唯讀）
(2, 4, 1, 1, 1),  -- 抽獎管理
(2, 5, 1, 1, 0),  -- 訂單管理（不可刪除）
(2, 41, 1, 1, 1), -- 抽獎列表
(2, 42, 1, 1, 1), -- 新增抽獎
(2, 43, 1, 1, 1), -- 獎池管理
(2, 51, 1, 1, 0), -- 訂單列表
(2, 52, 1, 1, 0); -- 訂單詳情

-- StoreEditor 權限（受限）
INSERT INTO role_menu (role_id, menu_id, can_view, can_edit, can_delete) VALUES
(3, 1, 1, 0, 0),  -- 儀表板（唯讀）
(3, 4, 1, 1, 0),  -- 抽獎管理（不可刪除）
(3, 5, 1, 0, 0),  -- 訂單管理（唯讀）
(3, 41, 1, 1, 0), -- 抽獎列表
(3, 42, 1, 1, 0), -- 新增抽獎（可新增不可刪除）
(3, 43, 1, 1, 0), -- 獎池管理
(3, 51, 1, 0, 0), -- 訂單列表（唯讀）
(3, 52, 1, 0, 0); -- 訂單詳情（唯讀）


-- =============================================
-- 四、管理員帳號 (admin_user)
-- =============================================

-- 系統管理員（密碼: admin123）
-- BCrypt Hash: $2a$10$N.zG.JlHRJ0X0L6J5P5/e.HJGBM7c3r0J0KQ9J6J5L7J0X0L6J5P5
INSERT INTO admin_user (id, username, password, email, display_name, phone, status, force_change_password, created_by) VALUES
(1, 'admin@kuji.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'admin@kuji.com', '系統管理員', '0900000000', 'ACTIVE', 0, NULL);

-- 測試店家負責人（密碼: Test1234）
INSERT INTO admin_user (id, username, password, email, display_name, phone, status, force_change_password, created_by, remark) VALUES
(2, 'owner@teststore.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'owner@teststore.com', '測試店家老闆', '0911111111', 'ACTIVE', 0, 1, '測試用店家負責人'),
(3, 'owner2@teststore.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'owner2@teststore.com', '第二間店家老闆', '0922222222', 'ACTIVE', 0, 1, '測試用店家負責人');

-- 測試店家編輯人員（密碼: Test1234）
INSERT INTO admin_user (id, username, password, email, display_name, phone, status, force_change_password, created_by, remark) VALUES
(4, 'editor@teststore.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'editor@teststore.com', '測試店家小編', '0933333333', 'ACTIVE', 0, 1, '測試用店家編輯人員');

-- 待啟用帳號（需首次修改密碼）
INSERT INTO admin_user (id, username, password, email, display_name, phone, status, force_change_password, created_by, remark) VALUES
(5, 'pending@teststore.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', 'pending@teststore.com', '待啟用帳號', '0944444444', 'PENDING', 1, 1, '測試首次登入流程');


-- =============================================
-- 五、使用者角色關聯 (admin_user_role)
-- =============================================

INSERT INTO admin_user_role (admin_user_id, role_id) VALUES
(1, 1),  -- admin@kuji.com -> Admin
(2, 2),  -- owner@teststore.com -> StoreOwner
(3, 2),  -- owner2@teststore.com -> StoreOwner
(4, 3),  -- editor@teststore.com -> StoreEditor
(5, 2);  -- pending@teststore.com -> StoreOwner


-- =============================================
-- 六、店家資料 (store)
-- =============================================

INSERT INTO store (id, owner_id, store_name, short_description, long_description, logo_url, cover_image_url, email, phone, address, facebook_url, instagram_url, line_id, business_hours, status, remark, updated_by) VALUES
(1, 2, 'KUJI 測試商店', '最好玩的抽獎商店', '這是一間專門販售各種精美獎品的抽獎商店，歡迎來試手氣！', 'https://via.placeholder.com/200', 'https://via.placeholder.com/1200x400', 'owner@teststore.com', '0911111111', '台北市信義區信義路五段7號', 'https://facebook.com/kujitest', 'https://instagram.com/kujitest', '@kujitest', '每日 10:00~22:00', 'ACTIVE', '測試用店家', 1),
(2, 3, '動漫周邊專賣店', '動漫迷必逛的抽獎店', '專營日本動漫周邊、公仔、模型等精品，採用一番賞抽獎機制。', 'https://via.placeholder.com/200', 'https://via.placeholder.com/1200x400', 'owner2@teststore.com', '0922222222', '台北市中山區南京東路三段168號', 'https://facebook.com/animestore', 'https://instagram.com/animestore', '@animestore', '每日 11:00~21:00', 'ACTIVE', '測試用店家', 1);


-- =============================================
-- 七、店家使用者關聯 (store_user)
-- =============================================

INSERT INTO store_user (store_id, admin_user_id, role_type) VALUES
(1, 2, 'OWNER'),   -- 測試商店 - owner@teststore.com
(1, 4, 'EDITOR'),  -- 測試商店 - editor@teststore.com
(2, 3, 'OWNER');   -- 動漫周邊專賣店 - owner2@teststore.com


-- =============================================
-- 八、前台會員 (user)
-- =============================================

INSERT INTO `user` (id, email, password, nickname, avatar_url, auth_provider, google_id, status, phone, gender, birthday, total_gold_points, total_bonus_points, created_at) VALUES
(1, 'user1@test.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', '測試會員A', 'https://via.placeholder.com/100', 'EMAIL', NULL, 'ACTIVE', '0955555555', 'M', '1990-01-15', 1000, 500, NOW()),
(2, 'user2@test.com', '$2a$10$EqKcp1GNhKz3/rAHxJzPJuJmJ9D2/1BkB7gGFl7J8hJb.JhJJJJJJJ', '測試會員B', 'https://via.placeholder.com/100', 'EMAIL', NULL, 'ACTIVE', '0966666666', 'F', '1995-06-20', 2500, 300, NOW()),
(3, 'googleuser@gmail.com', NULL, 'Google 測試會員', 'https://via.placeholder.com/100', 'GOOGLE', 'google_oauth_id_12345', 'ACTIVE', NULL, NULL, NULL, 500, 100, NOW());


-- =============================================
-- 九、抽獎活動 (lottery)
-- =============================================

INSERT INTO lottery (id, store_id, title, description, category, sub_category, status, original_price, single_draw_price, five_draw_price, ten_draw_price, total_quantity, remaining_quantity, protection_count, is_public, is_featured, start_date, end_date, main_image_url, created_by, created_at) VALUES
(1, 1, '鬼滅之刃一番賞', '超人氣鬼滅之刃一番賞，多款精美公仔等你來抽！', 'OFFICIAL_ICHIBAN', 'LOTTERY_MODE', 'ON_SHELF', 800, 80, 380, 720, 80, 65, 10, 1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'https://via.placeholder.com/400x300', 2, NOW()),
(2, 1, '咒術迴戰刮刮樂', '咒術迴戰限定刮刮樂，每張都有獎！', 'OFFICIAL_ICHIBAN', 'SCRATCH_CARD_MODE', 'ON_SHELF', 600, 60, 280, 540, 100, 100, 0, 1, 0, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 'https://via.placeholder.com/400x300', 2, NOW()),
(3, 2, '初音未來限定賞', '初音未來 15 週年紀念限定賞', 'GACHA', 'LOTTERY_MODE', 'DRAFT', 1200, 120, 560, 1080, 50, 50, 5, 0, 0, DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 37 DAY), 'https://via.placeholder.com/400x300', 3, NOW());


-- =============================================
-- 十、獎品資料 (lottery_prize)
-- =============================================

-- 鬼滅之刃一番賞獎品
INSERT INTO lottery_prize (lottery_id, level, name, description, total_quantity, remaining_quantity, image_url, order_num) VALUES
(1, 'A', '炭治郎公仔（大）', '約 25cm 高品質公仔', 1, 1, 'https://via.placeholder.com/200', 1),
(1, 'B', '禰豆子公仔（大）', '約 25cm 高品質公仔', 1, 1, 'https://via.placeholder.com/200', 2),
(1, 'C', '善逸公仔', '約 18cm 精緻公仔', 3, 3, 'https://via.placeholder.com/200', 3),
(1, 'D', '伊之助公仔', '約 18cm 精緻公仔', 5, 4, 'https://via.placeholder.com/200', 4),
(1, 'E', '壓克力立牌', '隨機角色壓克力立牌', 20, 18, 'https://via.placeholder.com/200', 5),
(1, 'F', '徽章組', '隨機 3 入徽章組', 30, 25, 'https://via.placeholder.com/200', 6),
(1, 'G', '貼紙包', '隨機貼紙包', 19, 12, 'https://via.placeholder.com/200', 7),
(1, 'LAST_PRIZE', '特別版炭治郎公仔', '最後一抽限定公仔', 1, 1, 'https://via.placeholder.com/200', 8);

-- 咒術迴戰刮刮樂獎品
INSERT INTO lottery_prize (lottery_id, level, name, description, total_quantity, remaining_quantity, image_url, order_num) VALUES
(2, 'A', '五條悟公仔', '約 20cm 公仔', 2, 2, 'https://via.placeholder.com/200', 1),
(2, 'B', '虎杖悠仁公仔', '約 18cm 公仔', 5, 5, 'https://via.placeholder.com/200', 2),
(2, 'C', '壓克力鑰匙圈', '隨機角色鑰匙圈', 20, 20, 'https://via.placeholder.com/200', 3),
(2, 'D', '透明資料夾', '隨機角色資料夾', 33, 33, 'https://via.placeholder.com/200', 4),
(2, 'E', '小貼紙', '隨機小貼紙', 40, 40, 'https://via.placeholder.com/200', 5);


-- =============================================
-- 十一、點數紀錄範例 (point_log)
-- =============================================

INSERT INTO point_log (user_id, point_type, operation_type, amount, balance_after, reference_type, reference_id, description, created_at) VALUES
(1, 'GOLD', 'DEPOSIT', 1000, 1000, 'PAYMENT', 1001, '儲值金點 NT$1000', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'BONUS', 'BONUS_GRANT', 500, 500, 'SYSTEM', NULL, '新會員首儲贈送紅利點', DATE_SUB(NOW(), INTERVAL 30 DAY)),
(1, 'GOLD', 'DRAW', -80, 920, 'LOTTERY_DRAW', 1, '抽獎消費：鬼滅之刃一番賞', DATE_SUB(NOW(), INTERVAL 25 DAY)),
(2, 'GOLD', 'DEPOSIT', 3000, 3000, 'PAYMENT', 1002, '儲值金點 NT$3000', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 'BONUS', 'BONUS_GRANT', 300, 300, 'SYSTEM', NULL, '儲值滿額贈送紅利點', DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 'GOLD', 'DRAW', -380, 2620, 'LOTTERY_DRAW', 1, '抽獎消費：鬼滅之刃一番賞 x5', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 'BONUS', 'DRAW', -120, 180, 'LOTTERY_DRAW', 1, '抽獎消費（紅利折抵）', DATE_SUB(NOW(), INTERVAL 15 DAY));


-- =============================================
-- 十二、抽獎紀錄範例 (lottery_draw_record)
-- =============================================

INSERT INTO lottery_draw_record (lottery_id, user_id, prize_id, prize_level, prize_name, draw_type, point_type, points_used, drawn_at) VALUES
(1, 1, 6, 'F', '徽章組', 'SINGLE', 'GOLD', 80, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 2, 5, 'E', '壓克力立牌', 'FIVE', 'GOLD', 76, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 2, 6, 'F', '徽章組', 'FIVE', 'GOLD', 76, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 2, 6, 'F', '徽章組', 'FIVE', 'GOLD', 76, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 2, 7, 'G', '貼紙包', 'FIVE', 'GOLD', 76, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 2, 7, 'G', '貼紙包', 'FIVE', 'BONUS', 76, DATE_SUB(NOW(), INTERVAL 15 DAY));


-- =============================================
-- 驗證查詢
-- =============================================

-- 檢查角色
-- SELECT * FROM role;

-- 檢查管理員帳號
-- SELECT id, username, display_name, status, force_change_password FROM admin_user;

-- 檢查角色關聯
-- SELECT au.username, r.name as role_name FROM admin_user au 
-- JOIN admin_user_role aur ON au.id = aur.admin_user_id 
-- JOIN role r ON aur.role_id = r.id;

-- 檢查店家與使用者關聯
-- SELECT s.store_name, au.username, su.role_type FROM store s
-- JOIN store_user su ON s.id = su.store_id
-- JOIN admin_user au ON su.admin_user_id = au.id;

-- 檢查抽獎活動
-- SELECT l.title, l.status, s.store_name FROM lottery l
-- JOIN store s ON l.store_id = s.id;
