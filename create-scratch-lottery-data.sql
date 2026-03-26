-- ================================================================================
-- 🎯 刮刮樂測試數據（2026-02-11 更新版）
-- ================================================================================
-- 說明：
-- 1. 包含 3 個刮刮樂商品範例
-- 2. playMode = 'SCRATCH_MODE'（刮刮樂模式）
-- 3. maxDraws > 獎品總數，多出來的籤位是「謝謝惠顧」
-- 4. 使用最新的表結構
--
-- 執行方式：
-- mysql -u root -p kuji_db < create-scratch-lottery-data.sql
-- ================================================================================

-- 查詢現有店家 ID（執行前請確認）
-- SELECT id, store_name FROM store;

-- 假設已有的店家 ID（請根據實際環境調整）
SET @store_id_1 = (SELECT id FROM store LIMIT 1);
SET @admin_user_id = (SELECT id FROM admin_user WHERE username = 'admin@kuji.com' LIMIT 1);

-- ================================================================================
-- 刮刮樂商品 1：海賊王刮刮樂（30抽，20個獎品 + 10個謝謝惠顧）
-- ================================================================================

SET @lottery_id_1 = UUID();

INSERT INTO lottery (
    id, store_id, title, description, 
    category, sub_category, play_mode,
    price_per_draw, discounted_price, auto_discount_enabled,
    allow_multi_draw, multi_draw_options,
    max_draws, total_draws,
    image_url, gallery_images,
    bonus_enabled, bonus_points_per_draw, bonus_cost_per_draw,
    status, hot_count, theme, tags, order_num, weight,
    created_by, remark,
    created_at, updated_at
) VALUES (
    @lottery_id_1,
    @store_id_1,
    '🏴‍☠️ 海賊王刮刮樂',
    '30 抽刮刮樂，含稀有A賞魯夫公仔！20個獎品 + 10個謝謝惠顧，每抽都有驚喜機會！',
    'CUSTOM_GACHA',              -- 自製賞
    'SCRATCH_MODE',              -- 刮刮樂型
    'SCRATCH_MODE',              -- 刮刮樂模式
    50,                          -- 每抽50元
    40,                          -- 大獎售完折扣價40元
    1,                           -- 啟用自動降價
    1,                           -- 允許多抽
    '5,10',                      -- 多抽選項：5抽、10抽
    30,                          -- 總共30抽
    0,                           -- 已抽0次
    'https://example.com/images/lottery/onepiece-scratch.jpg',
    'https://example.com/gallery/op1.jpg,https://example.com/gallery/op2.jpg',
    1,                           -- 啟用紅利
    5,                           -- 每抽贈送5紅利
    NULL,                        -- 不支援紅利抵抗
    'ON_SHELF',                  -- 已上架
    25,                          -- 熱度25
    '熱血冒險',                   -- 主題
    '海賊王,動漫,刮刮樂',          -- 標籤
    1,                           -- 排序1
    100,                         -- 權重100
    @admin_user_id,
    '測試刮刮樂商品 - 海賊王主題',
    NOW(),
    NOW()
);

-- 海賊王刮刮樂獎品（總共 20 個實體獎品）
INSERT INTO lottery_prize (
    id, lottery_id, 
    level, name, description, image_url,
    prize_type, point_value,
    quantity, remaining, weight,
    is_grand_prize, is_last_prize,
    order_num,
    created_at, updated_at
) VALUES
    -- A 賞：魯夫公仔（大獎）
    (UUID(), @lottery_id_1, 
     'A', '魯夫 PVC 公仔（珍藏版）', '15cm 經典造型，附展示座', 'https://example.com/prize/luffy-figure.jpg',
     'FIGURE', 0,
     1, 1, 100,
     1, 0,  -- is_grand_prize = 1
     1,
     NOW(), NOW()),
    
    -- B 賞：索隆武士刀模型（準大獎）
    (UUID(), @lottery_id_1,
     'B', '索隆 三刀流模型組', '可拆卸武士刀三件套', 'https://example.com/prize/zoro-sword.jpg',
     'GOODS', 0,
     2, 2, 100,
     0, 0,
     2,
     NOW(), NOW()),
    
    -- C 賞：娜美透明資料夾
    (UUID(), @lottery_id_1,
     'C', '娜美 透明資料夾（5入）', 'A4尺寸，精美印刷', 'https://example.com/prize/nami-folder.jpg',
     'GOODS', 0,
     5, 5, 100,
     0, 0,
     3,
     NOW(), NOW()),
    
    -- D 賞：海賊王徽章
    (UUID(), @lottery_id_1,
     'D', '海賊王角色徽章（隨機）', '直徑5cm金屬徽章', 'https://example.com/prize/badge.jpg',
     'GOODS', 0,
     7, 7, 100,
     0, 0,
     4,
     NOW(), NOW()),
    
    -- E 賞：千陽號橡皮擦
    (UUID(), @lottery_id_1,
     'E', '千陽號造型橡皮擦', '可愛迷你尺寸', 'https://example.com/prize/eraser.jpg',
     'GOODS', 0,
     5, 5, 100,
     0, 0,
     5,
     NOW(), NOW());

-- ================================================================================
-- 刮刮樂商品 2：寶可夢刮刮樂（50抽，30個獎品 + 20個謝謝惠顧）
-- ================================================================================

SET @lottery_id_2 = UUID();

INSERT INTO lottery (
    id, store_id, title, description,
    category, sub_category, play_mode,
    price_per_draw, discounted_price, auto_discount_enabled,
    allow_multi_draw, multi_draw_options,
    max_draws, total_draws,
    image_url, gallery_images,
    bonus_enabled, bonus_points_per_draw, bonus_cost_per_draw,
    status, hot_count, theme, tags, order_num, weight,
    created_by, remark,
    created_at, updated_at
) VALUES (
    @lottery_id_2,
    @store_id_1,
    '⚡ 寶可夢刮刮樂',
    '50 抽刮刮樂，皮卡丘、伊布等人氣角色週邊！30個獎品 + 20個謝謝惠顧',
    'CUSTOM_GACHA',
    'SCRATCH_MODE',
    'SCRATCH_MODE',
    80,                          -- 每抽80元
    65,                          -- 大獎售完折扣價65元
    1,                           -- 啟用自動降價
    1,                           -- 允許多抽
    '5,10,20',                   -- 多抽選項
    50,                          -- 總共50抽
    0,
    'https://example.com/images/lottery/pokemon-scratch.jpg',
    'https://example.com/gallery/pm1.jpg,https://example.com/gallery/pm2.jpg,https://example.com/gallery/pm3.jpg',
    1,
    8,                           -- 每抽贈送8紅利
    NULL,
    'ON_SHELF',
    38,                          -- 熱度38
    '可愛萌寵',
    '寶可夢,Pokemon,刮刮樂',
    2,
    95,
    @admin_user_id,
    '測試刮刮樂商品 - 寶可夢主題',
    NOW(),
    NOW()
);

-- 寶可夢刮刮樂獎品（總共 30 個實體獎品）
INSERT INTO lottery_prize (
    id, lottery_id,
    level, name, description, image_url,
    prize_type, point_value,
    quantity, remaining, weight,
    is_grand_prize, is_last_prize,
    order_num,
    created_at, updated_at
) VALUES
    -- A 賞：皮卡丘玩偶
    (UUID(), @lottery_id_2,
     'A', '皮卡丘絨毛玩偶（30cm）', '超萌表情，柔軟觸感', 'https://example.com/prize/pikachu-plush.jpg',
     'FIGURE', 0,
     2, 2, 100,
     1, 0,
     1,
     NOW(), NOW()),
    
    -- B 賞：伊布進化系列模型
    (UUID(), @lottery_id_2,
     'B', '伊布進化系列盒玩（隨機）', '9款伊布家族隨機1款', 'https://example.com/prize/eevee-set.jpg',
     'FIGURE', 0,
     3, 3, 100,
     0, 0,
     2,
     NOW(), NOW()),
    
    -- C 賞：寶可夢球造型收納盒
    (UUID(), @lottery_id_2,
     'C', '精靈球造型收納盒', '可開啟式設計，實用收納', 'https://example.com/prize/pokeball-box.jpg',
     'GOODS', 0,
     5, 5, 100,
     0, 0,
     3,
     NOW(), NOW()),
    
    -- D 賞：寶可夢卡牌包
    (UUID(), @lottery_id_2,
     'D', '寶可夢卡牌擴充包（5張裝）', '含稀有卡機率', 'https://example.com/prize/pokemon-card.jpg',
     'GOODS', 0,
     10, 10, 100,
     0, 0,
     4,
     NOW(), NOW()),
    
    -- E 賞：寶可夢貼紙組
    (UUID(), @lottery_id_2,
     'E', '寶可夢造型貼紙（10入）', '防水材質，可重覆貼', 'https://example.com/prize/stickers.jpg',
     'GOODS', 0,
     10, 10, 100,
     0, 0,
     5,
     NOW(), NOW());

-- ================================================================================
-- 刮刮樂商品 3：鬼滅之刃刮刮樂（100抽，40個獎品 + 60個謝謝惠顧）
-- ================================================================================

SET @lottery_id_3 = UUID();

INSERT INTO lottery (
    id, store_id, title, description,
    category, sub_category, play_mode,
    price_per_draw, discounted_price, auto_discount_enabled,
    allow_multi_draw, multi_draw_options,
    max_draws, total_draws,
    image_url, gallery_images,
    bonus_enabled, bonus_points_per_draw, bonus_cost_per_draw,
    status, hot_count, theme, tags, order_num, weight,
    created_by, remark,
    created_at, updated_at
) VALUES (
    @lottery_id_3,
    @store_id_1,
    '⚔️ 鬼滅之刃刮刮樂',
    '100 抽大型刮刮樂！炭治郎、禰豆子等人氣角色週邊，40個獎品 + 60個謝謝惠顧',
    'CUSTOM_GACHA',
    'SCRATCH_MODE',
    'SCRATCH_MODE',
    60,                          -- 每抽60元
    NULL,                        -- 不設折扣價
    0,                           -- 不啟用自動降價
    1,
    '5,10,20,50',                -- 多抽選項
    100,                         -- 總共100抽
    0,
    'https://example.com/images/lottery/kimetsu-scratch.jpg',
    'https://example.com/gallery/km1.jpg,https://example.com/gallery/km2.jpg',
    1,
    6,
    NULL,
    'ON_SHELF',
    50,                          -- 熱度50（超高人氣）
    '日式和風',
    '鬼滅之刃,Kimetsu,刮刮樂',
    3,
    120,
    @admin_user_id,
    '測試刮刮樂商品 - 鬼滅之刃主題（大型100抽）',
    NOW(),
    NOW()
);

-- 鬼滅之刃刮刮樂獎品（總共 40 個實體獎品）
INSERT INTO lottery_prize (
    id, lottery_id,
    level, name, description, image_url,
    prize_type, point_value,
    quantity, remaining, weight,
    is_grand_prize, is_last_prize,
    order_num,
    created_at, updated_at
) VALUES
    -- A 賞：炭治郎景品公仔
    (UUID(), @lottery_id_3,
     'A', '竈門炭治郎景品公仔（25cm）', '電影版造型，附底座', 'https://example.com/prize/tanjiro-figure.jpg',
     'FIGURE', 0,
     1, 1, 100,
     1, 0,
     1,
     NOW(), NOW()),
    
    -- B 賞：禰豆子模型
    (UUID(), @lottery_id_3,
     'B', '竈門禰豆子 Q版模型', '可動關節設計', 'https://example.com/prize/nezuko-figure.jpg',
     'FIGURE', 0,
     2, 2, 100,
     0, 0,
     2,
     NOW(), NOW()),
    
    -- C 賞：呼吸法特效配件組
    (UUID(), @lottery_id_3,
     'C', '呼吸法特效配件組', '適用於公仔，含透明支架', 'https://example.com/prize/effect-parts.jpg',
     'GOODS', 0,
     3, 3, 100,
     0, 0,
     3,
     NOW(), NOW()),
    
    -- D 賞：角色壓克力立牌
    (UUID(), @lottery_id_3,
     'D', '鬼滅角色壓克力立牌（隨機）', '12款角色隨機1款', 'https://example.com/prize/acrylic-stand.jpg',
     'GOODS', 0,
     10, 10, 100,
     0, 0,
     4,
     NOW(), NOW()),
    
    -- E 賞：和紙膠帶組
    (UUID(), @lottery_id_3,
     'E', '鬼滅和紙膠帶（3入組）', '精美和風圖案', 'https://example.com/prize/washi-tape.jpg',
     'GOODS', 0,
     12, 12, 100,
     0, 0,
     5,
     NOW(), NOW()),
    
    -- F 賞：角色徽章
    (UUID(), @lottery_id_3,
     'F', '鬼滅角色徽章（隨機2入）', '金屬材質，附別針', 'https://example.com/prize/badge-set.jpg',
     'GOODS', 0,
     12, 12, 100,
     0, 0,
     6,
     NOW(), NOW());

-- ================================================================================
-- 生成刮刮樂籤位（lottery_ticket）
-- ================================================================================

-- 🏴‍☠️ 海賊王刮刮樂籤位（30筆：20個獎品 + 10個謝謝惠顧）
-- A 賞：1 個（編號 1）
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_1, 1, lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_1 AND lp.level = 'A'
LIMIT 1;

-- B 賞：2 個（編號 2-3）
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_1, 2 + (@row_num := @row_num + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num := -1) r
WHERE lp.lottery_id = @lottery_id_1 AND lp.level = 'B' AND lp.quantity = 2
LIMIT 2;

-- C 賞：5 個（編號 4-8）
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_1, 4 + (@row_num2 := @row_num2 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num2 := -1) r
WHERE lp.lottery_id = @lottery_id_1 AND lp.level = 'C' AND lp.quantity = 5
LIMIT 5;

-- D 賞：7 個（編號 9-15）
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_1, 9 + (@row_num3 := @row_num3 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num3 := -1) r
WHERE lp.lottery_id = @lottery_id_1 AND lp.level = 'D' AND lp.quantity = 7
LIMIT 7;

-- E 賞：5 個（編號 16-20）
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_1, 16 + (@row_num4 := @row_num4 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num4 := -1) r
WHERE lp.lottery_id = @lottery_id_1 AND lp.level = 'E' AND lp.quantity = 5
LIMIT 5;

-- 謝謝惠顧：10 個（編號 21-30）
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
VALUES
    (UUID(), @lottery_id_1, 21, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 22, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 23, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 24, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 25, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 26, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 27, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 28, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 29, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()),
    (UUID(), @lottery_id_1, 30, NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW());

-- 隨機打亂海賊王籤位順序（模擬隨機分配）
SET @counter := 0;
UPDATE lottery_ticket lt
LEFT JOIN (
    SELECT id, (@counter := @counter + 1) AS new_number
    FROM lottery_ticket
    WHERE lottery_id = @lottery_id_1
    ORDER BY RAND()
) shuffled ON lt.id = shuffled.id
SET lt.ticket_number = shuffled.new_number
WHERE lt.lottery_id = @lottery_id_1;

-- ⚡ 寶可夢刮刮樂籤位（50筆：30個獎品 + 20個謝謝惠顧）
-- A 賞：2 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_2, (@row_num5 := @row_num5 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num5 := 0) r
WHERE lp.lottery_id = @lottery_id_2 AND lp.level = 'A' AND lp.quantity = 2
LIMIT 2;

-- B 賞：3 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_2, (@row_num6 := @row_num6 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num6 := 2) r
WHERE lp.lottery_id = @lottery_id_2 AND lp.level = 'B' AND lp.quantity = 3
LIMIT 3;

-- C 賞：5 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_2, (@row_num7 := @row_num7 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num7 := 5) r
WHERE lp.lottery_id = @lottery_id_2 AND lp.level = 'C' AND lp.quantity = 5
LIMIT 5;

-- D 賞：10 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_2, (@row_num8 := @row_num8 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num8 := 10) r
WHERE lp.lottery_id = @lottery_id_2 AND lp.level = 'D' AND lp.quantity = 10
LIMIT 10;

-- E 賞：10 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_2, (@row_num9 := @row_num9 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num9 := 20) r
WHERE lp.lottery_id = @lottery_id_2 AND lp.level = 'E' AND lp.quantity = 10
LIMIT 10;

-- 謝謝惠顧：20 個（編號 31-50）
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_2, 30 + (@thanks_num := @thanks_num + 1), NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()
FROM (SELECT @thanks_num := 0) init, (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20) nums;

-- 隨機打亂寶可夢籤位順序
SET @counter2 := 0;
UPDATE lottery_ticket lt
LEFT JOIN (
    SELECT id, (@counter2 := @counter2 + 1) AS new_number
    FROM lottery_ticket
    WHERE lottery_id = @lottery_id_2
    ORDER BY RAND()
) shuffled ON lt.id = shuffled.id
SET lt.ticket_number = shuffled.new_number
WHERE lt.lottery_id = @lottery_id_2;

-- ⚔️ 鬼滅之刃刮刮樂籤位（100筆：40個獎品 + 60個謝謝惠顧）
-- A 賞：1 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_3, 1, lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_3 AND lp.level = 'A'
LIMIT 1;

-- B 賞：2 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_3, 2 + (@row_num10 := @row_num10 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num10 := -1) r
WHERE lp.lottery_id = @lottery_id_3 AND lp.level = 'B' AND lp.quantity = 2
LIMIT 2;

-- C 賞：3 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_3, 4 + (@row_num11 := @row_num11 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num11 := -1) r
WHERE lp.lottery_id = @lottery_id_3 AND lp.level = 'C' AND lp.quantity = 3
LIMIT 3;

-- D 賞：10 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_3, 7 + (@row_num12 := @row_num12 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num12 := -1) r
WHERE lp.lottery_id = @lottery_id_3 AND lp.level = 'D' AND lp.quantity = 10
LIMIT 10;

-- E 賞：12 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_3, 17 + (@row_num13 := @row_num13 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num13 := -1) r
WHERE lp.lottery_id = @lottery_id_3 AND lp.level = 'E' AND lp.quantity = 12
LIMIT 12;

-- F 賞：12 個
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_3, 29 + (@row_num14 := @row_num14 + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @row_num14 := -1) r
WHERE lp.lottery_id = @lottery_id_3 AND lp.level = 'F' AND lp.quantity = 12
LIMIT 12;

-- 謝謝惠顧：60 個（編號 41-100）
-- 使用 UNION 生成 60 筆記錄
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_3, 40 + (@thanks_num2 := @thanks_num2 + 1), NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()
FROM (SELECT @thanks_num2 := 0) init,
(
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
) t1,
(
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
) t2
LIMIT 60;

-- 隨機打亂鬼滅之刃籤位順序
SET @counter3 := 0;
UPDATE lottery_ticket lt
LEFT JOIN (
    SELECT id, (@counter3 := @counter3 + 1) AS new_number
    FROM lottery_ticket
    WHERE lottery_id = @lottery_id_3
    ORDER BY RAND()
) shuffled ON lt.id = shuffled.id
SET lt.ticket_number = shuffled.new_number
WHERE lt.lottery_id = @lottery_id_3;

-- ================================================================================
-- 查詢結果驗證
-- ================================================================================
SELECT '=== 刮刮樂商品列表 ===' AS '';
SELECT 
    id,
    title,
    category,
    sub_category,
    play_mode,
    price_per_draw,
    max_draws,
    total_draws,
    status,
    hot_count
FROM lottery 
WHERE play_mode = 'SCRATCH_MODE'
ORDER BY order_num;

SELECT '=== 獎品統計 ===' AS '';
SELECT 
    l.title AS '商品名稱',
    COUNT(lp.id) AS '獎品種類數',
    SUM(lp.quantity) AS '總獎品數',
    l.max_draws AS '總抽數',
    (l.max_draws - SUM(lp.quantity)) AS '謝謝惠顧數量'
FROM lottery l
LEFT JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.play_mode = 'SCRATCH_MODE'
GROUP BY l.id, l.title, l.max_draws
ORDER BY l.order_num;

SELECT '=== 籤位統計 ===' AS '';
SELECT 
    l.title AS '商品名稱',
    COUNT(lt.id) AS '總籤位數',
    SUM(CASE WHEN lt.prize_level != 'THANKS' THEN 1 ELSE 0 END) AS '獎品籤位數',
    SUM(CASE WHEN lt.prize_level = 'THANKS' THEN 1 ELSE 0 END) AS '謝謝惠顧數',
    l.max_draws AS '預期總數'
FROM lottery l
LEFT JOIN lottery_ticket lt ON l.id = lt.lottery_id
WHERE l.play_mode = 'SCRATCH_MODE'
GROUP BY l.id, l.title, l.max_draws
ORDER BY l.order_num;

SELECT '=== 詳細獎品列表 ===' AS '';
SELECT 
    l.title AS '商品',
    lp.level AS '等級',
    lp.name AS '獎品名稱',
    lp.quantity AS '數量',
    lp.remaining AS '剩餘',
    lp.is_grand_prize AS '是否大獎'
FROM lottery l
INNER JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.play_mode = 'SCRATCH_MODE'
ORDER BY l.order_num, lp.order_num;

SELECT '=== 籤位詳情（前10個）===' AS '';
SELECT 
    l.title AS '商品',
    lt.ticket_number AS '號碼',
    lt.prize_level AS '獎品等級',
    CASE 
        WHEN lt.prize_level = 'THANKS' THEN '謝謝惠顧'
        ELSE lp.name
    END AS '獎品名稱',
    lt.status AS '狀態'
FROM lottery l
INNER JOIN lottery_ticket lt ON l.id = lt.lottery_id
LEFT JOIN lottery_prize lp ON lt.prize_id = lp.id
WHERE l.play_mode = 'SCRATCH_MODE'
ORDER BY l.order_num, lt.ticket_number
LIMIT 10;

-- ================================================================================
-- 完成提示
-- ================================================================================
SELECT '✅ 刮刮樂測試數據創建完成！' AS '';
SELECT CONCAT('📊 總共創建了 ', COUNT(*), ' 個刮刮樂商品') AS '' FROM lottery WHERE play_mode = 'SCRATCH_MODE';
SELECT CONCAT('🎁 總共創建了 ', COUNT(*), ' 個獎品項目') AS '' FROM lottery_prize WHERE lottery_id IN (SELECT id FROM lottery WHERE play_mode = 'SCRATCH_MODE');
SELECT CONCAT('🎫 總共創建了 ', COUNT(*), ' 個籤位（ticket）') AS '' FROM lottery_ticket WHERE lottery_id IN (SELECT id FROM lottery WHERE play_mode = 'SCRATCH_MODE');

-- 顯示每個商品的籤位分布
SELECT '=== 各商品籤位分布 ===' AS '';
SELECT 
    l.title AS '商品名稱',
    COUNT(lt.id) AS '總籤位數',
    SUM(CASE WHEN lt.prize_level = 'A' THEN 1 ELSE 0 END) AS 'A賞',
    SUM(CASE WHEN lt.prize_level = 'B' THEN 1 ELSE 0 END) AS 'B賞',
    SUM(CASE WHEN lt.prize_level = 'C' THEN 1 ELSE 0 END) AS 'C賞',
    SUM(CASE WHEN lt.prize_level = 'D' THEN 1 ELSE 0 END) AS 'D賞',
    SUM(CASE WHEN lt.prize_level = 'E' THEN 1 ELSE 0 END) AS 'E賞',
    SUM(CASE WHEN lt.prize_level = 'F' THEN 1 ELSE 0 END) AS 'F賞',
    SUM(CASE WHEN lt.prize_level = 'THANKS' THEN 1 ELSE 0 END) AS '謝謝惠顧'
FROM lottery l
INNER JOIN lottery_ticket lt ON l.id = lt.lottery_id
WHERE l.play_mode = 'SCRATCH_MODE'
GROUP BY l.id, l.title
ORDER BY l.order_num;
