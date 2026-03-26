-- ================================================================================
-- 🎫 為已存在的刮刮樂商品補上籤位（lottery_ticket）
-- ================================================================================
-- 說明：
-- 1. 用於已經創建的刮刮樂商品，但缺少籤位記錄的情況
-- 2. 根據商品的 max_draws 和 lottery_prize 自動生成對應數量的籤位
-- 3. 獎品會隨機分配到籤位，剩餘的是「謝謝惠顧」
--
-- 執行方式：
-- mysql -u root -p kuji_db < add-scratch-lottery-tickets.sql
-- ================================================================================

-- 設定商品 ID（根據您提供的資訊）
SET @lottery_id_onepiece = '638aa8ce-075c-11f1-bab7-0a7ddf3d3fc1';  -- 🏴‍☠️ 海賊王刮刮樂
SET @lottery_id_pokemon = '63a02abc-075c-11f1-bab7-0a7ddf3d3fc1';   -- ⚡ 寶可夢刮刮樂
SET @lottery_id_kimetsu = '63b4278f-075c-11f1-bab7-0a7ddf3d3fc1';   -- ⚔️ 鬼滅之刃刮刮樂

-- 檢查商品是否存在
SELECT '=== 檢查商品資訊 ===' AS '';
SELECT id, title, max_draws, play_mode, status
FROM lottery
WHERE id IN (@lottery_id_onepiece, @lottery_id_pokemon, @lottery_id_kimetsu);

-- 檢查商品的獎品配置
SELECT '=== 檢查獎品配置 ===' AS '';
SELECT 
    l.title AS '商品名稱',
    lp.level AS '等級',
    lp.name AS '獎品名稱',
    lp.quantity AS '數量'
FROM lottery l
INNER JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.id IN (@lottery_id_onepiece, @lottery_id_pokemon, @lottery_id_kimetsu)
ORDER BY l.title, lp.order_num;

-- 檢查是否已經有籤位（避免重複建立）
SELECT '=== 檢查現有籤位 ===' AS '';
SELECT 
    lottery_id,
    COUNT(*) AS '現有籤位數'
FROM lottery_ticket
WHERE lottery_id IN (@lottery_id_onepiece, @lottery_id_pokemon, @lottery_id_kimetsu)
GROUP BY lottery_id;

-- ================================================================================
-- 如果以上檢查確認無誤，請繼續執行以下腳本
-- ================================================================================

-- 🏴‍☠️ 海賊王刮刮樂籤位生成
-- 先刪除可能存在的舊籤位
DELETE FROM lottery_ticket WHERE lottery_id = @lottery_id_onepiece;

-- 為每個獎品等級生成籤位
-- A 賞
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT 
    UUID(), 
    @lottery_id_onepiece, 
    (@ticket_num_op_a := @ticket_num_op_a + 1), 
    lp.id, 
    lp.level, 
    'AVAILABLE', 
    0, 
    NOW(), 
    NOW()
FROM lottery_prize lp, (SELECT @ticket_num_op_a := 0) r
WHERE lp.lottery_id = @lottery_id_onepiece AND lp.level = 'A'
LIMIT 1;

-- B 賞
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT 
    UUID(), 
    @lottery_id_onepiece, 
    (@ticket_num_op_b := @ticket_num_op_b + 1), 
    lp.id, 
    lp.level, 
    'AVAILABLE', 
    0, 
    NOW(), 
    NOW()
FROM lottery_prize lp, (SELECT @ticket_num_op_b := (SELECT COUNT(*) FROM lottery_ticket WHERE lottery_id = @lottery_id_onepiece)) r
WHERE lp.lottery_id = @lottery_id_onepiece AND lp.level = 'B'
LIMIT 2;

-- C 賞
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT 
    UUID(), 
    @lottery_id_onepiece, 
    (@ticket_num_op_c := @ticket_num_op_c + 1), 
    lp.id, 
    lp.level, 
    'AVAILABLE', 
    0, 
    NOW(), 
    NOW()
FROM lottery_prize lp, (SELECT @ticket_num_op_c := (SELECT COUNT(*) FROM lottery_ticket WHERE lottery_id = @lottery_id_onepiece)) r
WHERE lp.lottery_id = @lottery_id_onepiece AND lp.level = 'C'
LIMIT 5;

-- D 賞
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT 
    UUID(), 
    @lottery_id_onepiece, 
    (@ticket_num_op_d := @ticket_num_op_d + 1), 
    lp.id, 
    lp.level, 
    'AVAILABLE', 
    0, 
    NOW(), 
    NOW()
FROM lottery_prize lp, (SELECT @ticket_num_op_d := (SELECT COUNT(*) FROM lottery_ticket WHERE lottery_id = @lottery_id_onepiece)) r
WHERE lp.lottery_id = @lottery_id_onepiece AND lp.level = 'D'
LIMIT 7;

-- E 賞
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT 
    UUID(), 
    @lottery_id_onepiece, 
    (@ticket_num_op_e := @ticket_num_op_e + 1), 
    lp.id, 
    lp.level, 
    'AVAILABLE', 
    0, 
    NOW(), 
    NOW()
FROM lottery_prize lp, (SELECT @ticket_num_op_e := (SELECT COUNT(*) FROM lottery_ticket WHERE lottery_id = @lottery_id_onepiece)) r
WHERE lp.lottery_id = @lottery_id_onepiece AND lp.level = 'E'
LIMIT 5;

-- 計算需要多少個「謝謝惠顧」
SET @current_count_op = (SELECT COUNT(*) FROM lottery_ticket WHERE lottery_id = @lottery_id_onepiece);
SET @max_draws_op = (SELECT max_draws FROM lottery WHERE id = @lottery_id_onepiece);
SET @thanks_needed_op = @max_draws_op - @current_count_op;

-- 生成「謝謝惠顧」籤位（如果需要的話）
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT 
    UUID(), 
    @lottery_id_onepiece, 
    @current_count_op + (@thanks_row_op := @thanks_row_op + 1), 
    NULL, 
    'THANKS', 
    'AVAILABLE', 
    0, 
    NOW(), 
    NOW()
FROM (SELECT @thanks_row_op := 0) init,
(
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
    UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 
    UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
    UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35 
    UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
    UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45 
    UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
    UNION ALL SELECT 51 UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54 UNION ALL SELECT 55 
    UNION ALL SELECT 56 UNION ALL SELECT 57 UNION ALL SELECT 58 UNION ALL SELECT 59 UNION ALL SELECT 60
    UNION ALL SELECT 61 UNION ALL SELECT 62 UNION ALL SELECT 63 UNION ALL SELECT 64 UNION ALL SELECT 65 
    UNION ALL SELECT 66 UNION ALL SELECT 67 UNION ALL SELECT 68 UNION ALL SELECT 69 UNION ALL SELECT 70
    UNION ALL SELECT 71 UNION ALL SELECT 72 UNION ALL SELECT 73 UNION ALL SELECT 74 UNION ALL SELECT 75 
    UNION ALL SELECT 76 UNION ALL SELECT 77 UNION ALL SELECT 78 UNION ALL SELECT 79 UNION ALL SELECT 80
    UNION ALL SELECT 81 UNION ALL SELECT 82 UNION ALL SELECT 83 UNION ALL SELECT 84 UNION ALL SELECT 85 
    UNION ALL SELECT 86 UNION ALL SELECT 87 UNION ALL SELECT 88 UNION ALL SELECT 89 UNION ALL SELECT 90
    UNION ALL SELECT 91 UNION ALL SELECT 92 UNION ALL SELECT 93 UNION ALL SELECT 94 UNION ALL SELECT 95 
    UNION ALL SELECT 96 UNION ALL SELECT 97 UNION ALL SELECT 98 UNION ALL SELECT 99 UNION ALL SELECT 100
) nums
LIMIT @thanks_needed_op;

-- 隨機打亂海賊王籤位順序
SET @counter_op := 0;
UPDATE lottery_ticket lt
LEFT JOIN (
    SELECT id, (@counter_op := @counter_op + 1) AS new_number
    FROM lottery_ticket
    WHERE lottery_id = @lottery_id_onepiece
    ORDER BY RAND()
) shuffled ON lt.id = shuffled.id
SET lt.ticket_number = shuffled.new_number
WHERE lt.lottery_id = @lottery_id_onepiece;

SELECT CONCAT('✅ 海賊王刮刮樂籤位生成完成：', COUNT(*), ' 個籤位') AS '' 
FROM lottery_ticket WHERE lottery_id = @lottery_id_onepiece;

-- ================================================================================
-- ⚡ 寶可夢刮刮樂籤位生成（使用相同邏輯）
-- ================================================================================
DELETE FROM lottery_ticket WHERE lottery_id = @lottery_id_pokemon;

-- A 賞 (2個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_pokemon, (@ticket_num_pm := @ticket_num_pm + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @ticket_num_pm := 0) r
WHERE lp.lottery_id = @lottery_id_pokemon AND lp.level = 'A' AND lp.quantity = 2
LIMIT 2;

-- B 賞 (3個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_pokemon, (@ticket_num_pm := @ticket_num_pm + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_pokemon AND lp.level = 'B' AND lp.quantity = 3
LIMIT 3;

-- C 賞 (5個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_pokemon, (@ticket_num_pm := @ticket_num_pm + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_pokemon AND lp.level = 'C' AND lp.quantity = 5
LIMIT 5;

-- D 賞 (10個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_pokemon, (@ticket_num_pm := @ticket_num_pm + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_pokemon AND lp.level = 'D' AND lp.quantity = 10
LIMIT 10;

-- E 賞 (10個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_pokemon, (@ticket_num_pm := @ticket_num_pm + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_pokemon AND lp.level = 'E' AND lp.quantity = 10
LIMIT 10;

-- 謝謝惠顧
SET @current_count_pm = (SELECT COUNT(*) FROM lottery_ticket WHERE lottery_id = @lottery_id_pokemon);
SET @max_draws_pm = (SELECT max_draws FROM lottery WHERE id = @lottery_id_pokemon);
SET @thanks_needed_pm = @max_draws_pm - @current_count_pm;

INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_pokemon, @current_count_pm + (@thanks_row_pm := @thanks_row_pm + 1), NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()
FROM (SELECT @thanks_row_pm := 0) init,
(
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
    UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 
    UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
    UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35 
    UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
    UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45 
    UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
    UNION ALL SELECT 51 UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54 UNION ALL SELECT 55 
    UNION ALL SELECT 56 UNION ALL SELECT 57 UNION ALL SELECT 58 UNION ALL SELECT 59 UNION ALL SELECT 60
) nums
LIMIT @thanks_needed_pm;

-- 隨機打亂
SET @counter_pm := 0;
UPDATE lottery_ticket lt
LEFT JOIN (
    SELECT id, (@counter_pm := @counter_pm + 1) AS new_number
    FROM lottery_ticket WHERE lottery_id = @lottery_id_pokemon ORDER BY RAND()
) shuffled ON lt.id = shuffled.id
SET lt.ticket_number = shuffled.new_number
WHERE lt.lottery_id = @lottery_id_pokemon;

SELECT CONCAT('✅ 寶可夢刮刮樂籤位生成完成：', COUNT(*), ' 個籤位') AS '' 
FROM lottery_ticket WHERE lottery_id = @lottery_id_pokemon;

-- ================================================================================
-- ⚔️ 鬼滅之刃刮刮樂籤位生成
-- ================================================================================
DELETE FROM lottery_ticket WHERE lottery_id = @lottery_id_kimetsu;

-- A 賞 (1個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_kimetsu, (@ticket_num_km := @ticket_num_km + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp, (SELECT @ticket_num_km := 0) r
WHERE lp.lottery_id = @lottery_id_kimetsu AND lp.level = 'A'
LIMIT 1;

-- B 賞 (2個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_kimetsu, (@ticket_num_km := @ticket_num_km + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_kimetsu AND lp.level = 'B' AND lp.quantity = 2
LIMIT 2;

-- C 賞 (3個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_kimetsu, (@ticket_num_km := @ticket_num_km + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_kimetsu AND lp.level = 'C' AND lp.quantity = 3
LIMIT 3;

-- D 賞 (10個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_kimetsu, (@ticket_num_km := @ticket_num_km + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_kimetsu AND lp.level = 'D' AND lp.quantity = 10
LIMIT 10;

-- E 賞 (12個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_kimetsu, (@ticket_num_km := @ticket_num_km + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_kimetsu AND lp.level = 'E' AND lp.quantity = 12
LIMIT 12;

-- F 賞 (12個)
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_kimetsu, (@ticket_num_km := @ticket_num_km + 1), lp.id, lp.level, 'AVAILABLE', 0, NOW(), NOW()
FROM lottery_prize lp
WHERE lp.lottery_id = @lottery_id_kimetsu AND lp.level = 'F' AND lp.quantity = 12
LIMIT 12;

-- 謝謝惠顧
SET @current_count_km = (SELECT COUNT(*) FROM lottery_ticket WHERE lottery_id = @lottery_id_kimetsu);
SET @max_draws_km = (SELECT max_draws FROM lottery WHERE id = @lottery_id_kimetsu);
SET @thanks_needed_km = @max_draws_km - @current_count_km;

INSERT INTO lottery_ticket (id, lottery_id, ticket_number, prize_id, prize_level, status, is_designated_prize, created_at, updated_at)
SELECT UUID(), @lottery_id_kimetsu, @current_count_km + (@thanks_row_km := @thanks_row_km + 1), NULL, 'THANKS', 'AVAILABLE', 0, NOW(), NOW()
FROM (SELECT @thanks_row_km := 0) init,
(
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
    UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15 
    UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20
    UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 
    UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29 UNION ALL SELECT 30
    UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34 UNION ALL SELECT 35 
    UNION ALL SELECT 36 UNION ALL SELECT 37 UNION ALL SELECT 38 UNION ALL SELECT 39 UNION ALL SELECT 40
    UNION ALL SELECT 41 UNION ALL SELECT 42 UNION ALL SELECT 43 UNION ALL SELECT 44 UNION ALL SELECT 45 
    UNION ALL SELECT 46 UNION ALL SELECT 47 UNION ALL SELECT 48 UNION ALL SELECT 49 UNION ALL SELECT 50
    UNION ALL SELECT 51 UNION ALL SELECT 52 UNION ALL SELECT 53 UNION ALL SELECT 54 UNION ALL SELECT 55 
    UNION ALL SELECT 56 UNION ALL SELECT 57 UNION ALL SELECT 58 UNION ALL SELECT 59 UNION ALL SELECT 60
    UNION ALL SELECT 61 UNION ALL SELECT 62 UNION ALL SELECT 63 UNION ALL SELECT 64 UNION ALL SELECT 65 
    UNION ALL SELECT 66 UNION ALL SELECT 67 UNION ALL SELECT 68 UNION ALL SELECT 69 UNION ALL SELECT 70
    UNION ALL SELECT 71 UNION ALL SELECT 72 UNION ALL SELECT 73 UNION ALL SELECT 74 UNION ALL SELECT 75 
    UNION ALL SELECT 76 UNION ALL SELECT 77 UNION ALL SELECT 78 UNION ALL SELECT 79 UNION ALL SELECT 80
    UNION ALL SELECT 81 UNION ALL SELECT 82 UNION ALL SELECT 83 UNION ALL SELECT 84 UNION ALL SELECT 85 
    UNION ALL SELECT 86 UNION ALL SELECT 87 UNION ALL SELECT 88 UNION ALL SELECT 89 UNION ALL SELECT 90
    UNION ALL SELECT 91 UNION ALL SELECT 92 UNION ALL SELECT 93 UNION ALL SELECT 94 UNION ALL SELECT 95 
    UNION ALL SELECT 96 UNION ALL SELECT 97 UNION ALL SELECT 98 UNION ALL SELECT 99 UNION ALL SELECT 100
) nums
LIMIT @thanks_needed_km;

-- 隨機打亂
SET @counter_km := 0;
UPDATE lottery_ticket lt
LEFT JOIN (
    SELECT id, (@counter_km := @counter_km + 1) AS new_number
    FROM lottery_ticket WHERE lottery_id = @lottery_id_kimetsu ORDER BY RAND()
) shuffled ON lt.id = shuffled.id
SET lt.ticket_number = shuffled.new_number
WHERE lt.lottery_id = @lottery_id_kimetsu;

SELECT CONCAT('✅ 鬼滅之刃刮刮樂籤位生成完成：', COUNT(*), ' 個籤位') AS '' 
FROM lottery_ticket WHERE lottery_id = @lottery_id_kimetsu;

-- ================================================================================
-- 最終驗證
-- ================================================================================
SELECT '=== 籤位生成結果 ===' AS '';
SELECT 
    l.title AS '商品名稱',
    l.max_draws AS '預期總數',
    COUNT(lt.id) AS '實際籤位數',
    SUM(CASE WHEN lt.prize_level != 'THANKS' THEN 1 ELSE 0 END) AS '獎品籤位',
    SUM(CASE WHEN lt.prize_level = 'THANKS' THEN 1 ELSE 0 END) AS '謝謝惠顧',
    CASE 
        WHEN COUNT(lt.id) = l.max_draws THEN '✅ 正確'
        ELSE '❌ 數量不符'
    END AS '狀態'
FROM lottery l
LEFT JOIN lottery_ticket lt ON l.id = lt.lottery_id
WHERE l.id IN (@lottery_id_onepiece, @lottery_id_pokemon, @lottery_id_kimetsu)
GROUP BY l.id, l.title, l.max_draws
ORDER BY l.title;

-- 顯示範例籤位（每個商品前5個）
SELECT '=== 籤位範例（每個商品前5個）===' AS '';
(
    SELECT l.title, lt.ticket_number, lt.prize_level, 
           CASE WHEN lt.prize_level = 'THANKS' THEN '謝謝惠顧' ELSE lp.name END AS prize_name
    FROM lottery l
    INNER JOIN lottery_ticket lt ON l.id = lt.lottery_id
    LEFT JOIN lottery_prize lp ON lt.prize_id = lp.id
    WHERE l.id = @lottery_id_onepiece
    ORDER BY lt.ticket_number
    LIMIT 5
)
UNION ALL
(
    SELECT l.title, lt.ticket_number, lt.prize_level, 
           CASE WHEN lt.prize_level = 'THANKS' THEN '謝謝惠顧' ELSE lp.name END AS prize_name
    FROM lottery l
    INNER JOIN lottery_ticket lt ON l.id = lt.lottery_id
    LEFT JOIN lottery_prize lp ON lt.prize_id = lp.id
    WHERE l.id = @lottery_id_pokemon
    ORDER BY lt.ticket_number
    LIMIT 5
)
UNION ALL
(
    SELECT l.title, lt.ticket_number, lt.prize_level, 
           CASE WHEN lt.prize_level = 'THANKS' THEN '謝謝惠顧' ELSE lp.name END AS prize_name
    FROM lottery l
    INNER JOIN lottery_ticket lt ON l.id = lt.lottery_id
    LEFT JOIN lottery_prize lp ON lt.prize_id = lp.id
    WHERE l.id = @lottery_id_kimetsu
    ORDER BY lt.ticket_number
    LIMIT 5
);

SELECT '✅ 所有籤位生成完成！' AS '';
