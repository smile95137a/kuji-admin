-- ================================================================================
-- 刮刮樂測試假資料
-- ================================================================================
-- 說明：
-- 1. 本檔案提供 3 個刮刮樂商品範例，用於測試刮刮樂功能
-- 2. 包含完整的獎品設定（A/B/C/D 獎 + 謝謝惠顧）
-- 3. maxDraws > 獎品總數，多出來的籤位是「謝謝惠顧」
-- 4. playMode 設為 'SCRATCH_MODE'（刮刮樂模式）
--
-- 執行方式：
-- mysql -u root -p kuji_db < scratch-lottery-test-data.sql
-- ================================================================================

-- 假設已有的店家 ID（請根據實際環境調整）
SET @store_id_1 = '550e8400-e29b-41d4-a716-446655440000';

-- ================================================================================
-- 刮刮樂商品 1：海賊王刮刮樂（30抽，20個獎品 + 10個謝謝惠顧）
-- ================================================================================

SET @lottery_id_1 = UUID();

INSERT INTO lottery (
    id, store_id, title, description, category, sub_category, play_mode,
    price_per_draw, max_draws, total_value, image_url, 
    bonus_points_per_draw, bonus_cost_per_draw,
    status, hot_count, theme, order_num, remark,
    created_at, updated_at
) VALUES (
    @lottery_id_1,
    @store_id_1,
    '海賊王刮刮樂',
    '30 抽刮刮樂，含稀有A賞魯夫公仔！20個獎品 + 10個謝謝惠顧',
    'CUSTOM_GACHA',           -- 自製賞
    'SCRATCH_MODE',           -- 刮刮樂型
    'SCRATCH_MODE',           -- 刮刮樂模式
    50,                       -- 每抽50元
    30,                       -- 總共30抽
    2500,                     -- 總價值2500元
    'https://example.com/images/scratch/onepiece.jpg',
    5,                        -- 每抽贈送5紅利
    NULL,                     -- 不支援紅利抽獎
    'ON_SHELF',               -- 已上架
    15,                       -- 熱度15
    '熱血冒險',
    1,
    '測試刮刮樂商品',
    NOW(),
    NOW()
);

-- 海賊王刮刮樂獎品（總共 20 個實體獎品）
INSERT INTO lottery_prize (id, lottery_id, level, name, type, quantity, remaining, weight, image_url, recycle_bonus, order_num, created_at, updated_at) VALUES
    (UUID(), @lottery_id_1, 'A', '魯夫 PVC 公仔（珍藏版）', 'PHYSICAL', 1, 1, 100, 'https://example.com/prize/luffy-figure.jpg', 500, 1, NOW(), NOW()),
    (UUID(), @lottery_id_1, 'B', '索隆 武士刀模型', 'PHYSICAL', 2, 2, 100, 'https://example.com/prize/zoro-sword.jpg', 300, 2, NOW(), NOW()),
    (UUID(), @lottery_id_1, 'C', '娜美 透明資料夾', 'PHYSICAL', 5, 5, 100, 'https://example.com/prize/nami-folder.jpg', 100, 3, NOW(), NOW()),
    (UUID(), @lottery_id_1, 'D', '海賊王徽章（隨機）', 'PHYSICAL', 7, 7, 100, 'https://example.com/prize/badge.jpg', 50, 4, NOW(), NOW()),
    (UUID(), @lottery_id_1, 'E', '千陽號橡皮擦', 'PHYSICAL', 5, 5, 100, 'https://example.com/prize/eraser.jpg', 30, 5, NOW(), NOW());

-- ================================================================================
-- 刮刮樂商品 2：寶可夢刮刮樂（50抽，30個獎品 + 20個謝謝惠顧）
-- ================================================================================

SET @lottery_id_2 = UUID();

INSERT INTO lottery (
    id, store_id, title, description, category, sub_category, play_mode,
    price_per_draw, max_draws, total_value, image_url,
    bonus_points_per_draw, bonus_cost_per_draw,
    status, hot_count, theme, order_num, remark,
    created_at, updated_at
) VALUES (
    @lottery_id_2,
    @store_id_1,
    '寶可夢刮刮樂',
    '50 抽刮刮樂，皮卡丘、伊布等人氣角色週邊！30個獎品 + 20個謝謝惠顧',
    'CUSTOM_GACHA',
    'SCRATCH_MODE',
    'SCRATCH_MODE',
    80,                       -- 每抽80元
    50,                       -- 總共50抽
    5000,                     -- 總價值5000元
    'https://example.com/images/scratch/pokemon.jpg',
    8,                        -- 每抽贈送8紅利
    NULL,
    'ON_SHELF',
    28,                       -- 熱度28
    '可愛萌系',
    2,
    '測試刮刮樂商品',
    NOW(),
    NOW()
);

-- 寶可夢刮刮樂獎品（總共 30 個實體獎品）
INSERT INTO lottery_prize (id, lottery_id, level, name, type, quantity, remaining, weight, image_url, recycle_bonus, order_num, created_at, updated_at) VALUES
    (UUID(), @lottery_id_2, 'A', '皮卡丘絨毛玩偶（大）', 'PHYSICAL', 2, 2, 100, 'https://example.com/prize/pikachu-plush-l.jpg', 800, 1, NOW(), NOW()),
    (UUID(), @lottery_id_2, 'B', '伊布毛巾', 'PHYSICAL', 3, 3, 100, 'https://example.com/prize/eevee-towel.jpg', 400, 2, NOW(), NOW()),
    (UUID(), @lottery_id_2, 'C', '寶可夢馬克杯', 'PHYSICAL', 8, 8, 100, 'https://example.com/prize/pokemon-mug.jpg', 150, 3, NOW(), NOW()),
    (UUID(), @lottery_id_2, 'D', '寶貝球鑰匙圈', 'PHYSICAL', 10, 10, 100, 'https://example.com/prize/pokeball-keychain.jpg', 80, 4, NOW(), NOW()),
    (UUID(), @lottery_id_2, 'E', '寶可夢貼紙組', 'PHYSICAL', 7, 7, 100, 'https://example.com/prize/stickers.jpg', 40, 5, NOW(), NOW());

-- ================================================================================
-- 刮刮樂商品 3：鬼滅之刃刮刮樂（40抽，25個獎品 + 15個謝謝惠顧）
-- ================================================================================

SET @lottery_id_3 = UUID();

INSERT INTO lottery (
    id, store_id, title, description, category, sub_category, play_mode,
    price_per_draw, max_draws, total_value, image_url,
    bonus_points_per_draw, bonus_cost_per_draw,
    status, hot_count, theme, order_num, remark,
    created_at, updated_at
) VALUES (
    @lottery_id_3,
    @store_id_1,
    '鬼滅之刃刮刮樂',
    '40 抽刮刮樂，炭治郎、禰豆子等鬼殺隊成員週邊！25個獎品 + 15個謝謝惠顧',
    'CUSTOM_GACHA',
    'SCRATCH_MODE',
    'SCRATCH_MODE',
    60,                       -- 每抽60元
    40,                       -- 總共40抽
    3500,                     -- 總價值3500元
    'https://example.com/images/scratch/kimetsu.jpg',
    6,                        -- 每抽贈送6紅利
    NULL,
    'DRAFT',                  -- 草稿狀態（測試上架功能用）
    0,
    '日本漫畫',
    3,
    '測試刮刮樂商品（草稿）',
    NOW(),
    NOW()
);

-- 鬼滅之刃刮刮樂獎品（總共 25 個實體獎品）
INSERT INTO lottery_prize (id, lottery_id, level, name, type, quantity, remaining, weight, image_url, recycle_bonus, order_num, created_at, updated_at) VALUES
    (UUID(), @lottery_id_3, 'A', '炭治郎 景品公仔', 'PHYSICAL', 1, 1, 100, 'https://example.com/prize/tanjiro-figure.jpg', 600, 1, NOW(), NOW()),
    (UUID(), @lottery_id_3, 'B', '禰豆子 亞克力立牌', 'PHYSICAL', 3, 3, 100, 'https://example.com/prize/nezuko-stand.jpg', 350, 2, NOW(), NOW()),
    (UUID(), @lottery_id_3, 'C', '鬼殺隊 馬克杯', 'PHYSICAL', 6, 6, 100, 'https://example.com/prize/corps-mug.jpg', 120, 3, NOW(), NOW()),
    (UUID(), @lottery_id_3, 'D', '日輪刀 書籤', 'PHYSICAL', 9, 9, 100, 'https://example.com/prize/sword-bookmark.jpg', 60, 4, NOW(), NOW()),
    (UUID(), @lottery_id_3, 'E', '鬼滅小卡（隨機）', 'PHYSICAL', 6, 6, 100, 'https://example.com/prize/card.jpg', 35, 5, NOW(), NOW());

-- ================================================================================
-- 資料驗證查詢
-- ================================================================================

-- 查看剛才建立的刮刮樂商品
SELECT 
    id,
    title,
    category,
    play_mode,
    price_per_draw,
    max_draws,
    status
FROM lottery 
WHERE title LIKE '%刮刮樂%'
ORDER BY order_num;

-- 查看各商品的獎品配置
SELECT 
    l.title AS '商品名稱',
    lp.level AS '獎項',
    lp.name AS '獎品名稱',
    lp.quantity AS '數量',
    lp.recycle_bonus AS '回收紅利'
FROM lottery l
JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.title LIKE '%刮刮樂%'
ORDER BY l.order_num, lp.order_num;

-- 統計各商品的獎品總數與謝謝惠顧數量
SELECT 
    l.title AS '商品名稱',
    l.max_draws AS '總抽數',
    SUM(lp.quantity) AS '獎品總數',
    (l.max_draws - SUM(lp.quantity)) AS '謝謝惠顧數量',
    CONCAT(ROUND((SUM(lp.quantity) / l.max_draws) * 100, 1), '%') AS '中獎率'
FROM lottery l
LEFT JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.title LIKE '%刮刮樂%'
GROUP BY l.id, l.title, l.max_draws
ORDER BY l.order_num;

-- ================================================================================
-- 說明文件
-- ================================================================================
-- 
-- ## 刮刮樂測試資料說明
--
-- ### 商品 1：海賊王刮刮樂
-- - 總抽數：30 抽
-- - 獎品數：20 個
-- - 謝謝惠顧：10 個（33.3%的籤位）
-- - 中獎率：66.7%
-- - 狀態：ON_SHELF（已上架）
--
-- ### 商品 2：寶可夢刮刮樂
-- - 總抽數：50 抽
-- - 獎品數：30 個
-- - 謝謝惠顧：20 個（40%的籤位）
-- - 中獎率：60%
-- - 狀態：ON_SHELF（已上架）
--
-- ### 商品 3：鬼滅之刃刮刮樂
-- - 總抽數：40 抽
-- - 獎品數：25 個
-- - 謝謝惠顧：15 個（37.5%的籤位）
-- - 中獎率：62.5%
-- - 狀態：DRAFT（草稿，可用於測試上架功能）
--
-- ## 測試步驟
--
-- 1. **執行 SQL 匯入資料**：
--    ```bash
--    mysql -u root -p kuji_db < scratch-lottery-test-data.sql
--    ```
--
-- 2. **啟動後端服務**：
--    ```bash
--    mvn spring-boot:run
--    ```
--
-- 3. **呼叫瀏覽 API 確認商品**：
--    ```bash
--    curl -X POST http://localhost:8080/api/lottery/browse/list \
--      -H "Content-Type: application/json" \
--      -d '{"condition":{"category":"CUSTOM_GACHA","subCategory":"SCRATCH_MODE"}}'
--    ```
--
-- 4. **測試刮刮樂抽獎**：
--    - 對於 ON_SHELF 狀態的商品（商品1、商品2）
--    - 呼叫 POST /lottery/draw/{lotteryId}/draw
--    - 可能會抽到獎品或「謝謝惠顧」
--
-- 5. **驗證籤位生成**：
--    - 查詢 lottery_ticket 表
--    - prize_id 為 NULL 的籤位就是「謝謝惠顧」
--    - prize_id 不為 NULL 的籤位有對應獎品
--
-- ## 注意事項
--
-- ⚠️ **店家 ID 需調整**：
-- - 本檔案假設店家 ID 為 `550e8400-e29b-41d4-a716-446655440000`
-- - 執行前請查詢現有店家：`SELECT id, store_name FROM store;`
-- - 修改檔案開頭的 `@store_id_1` 變數為實際店家 ID
--
-- ⚠️ **籤位需手動生成**：
-- - 匯入商品後，需要呼叫「上架」API 才會生成籤位
-- - 或者手動呼叫 LotteryTicketService.generateTickets(lotteryId)
-- - 草稿狀態的商品（商品3）需先改為 ON_SHELF 才能抽獎
--
-- ================================================================================
