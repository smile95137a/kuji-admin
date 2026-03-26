-- ================================================================
-- 刮刮樂測試資料（完整版，含 lottery_ticket + revealed_number）
-- Generated: 2026-02-26
-- 
-- 包含：
--   範例一：SCRATCH_STORE（店家指定大獎）10 抽，含完整 10 筆 lottery_ticket
--   範例二：SCRATCH_PLAYER（開套玩家指定大獎）10 抽，含完整 10 筆 lottery_ticket
-- 
-- 保護時間規則（protection_draws / protection_minutes）：
--   ❌ 扭蛋（LOTTERY_MODE + category=GACHA）→ 不需要保護（每次獨立隨機）
--   ✅ 一番賞（LOTTERY_MODE + category=ICHIBAN）→ 需要保護時間
--   ✅ 刮刮樂（SCRATCH_MODE）→ 需要保護時間（下方兩個範例皆已設置）
-- 
-- 執行前請先確認 store_id：
--   SELECT id, name FROM store LIMIT 5;
-- ================================================================

SET @store_id = (SELECT id FROM store ORDER BY created_at LIMIT 1);

-- ================================================================
-- 範例一：SCRATCH_STORE（店家預先指定大獎位置）
-- ================================================================
--
-- 10 張刮刮樂卡：
--   ticket_number（物理序號）→ revealed_number（刮開號碼）shuffle 映射：
--   1→8、2→3*、3→10、4→1、5→7*、6→5、7→2、8→9、9→6、10→4
--   (* = 大獎位置，designated_prize_numbers = '[3, 7]')
--
--   實際獎品分配：
--     revealed=3 (ticket 2)  → A 賞大獎，is_designated_prize=1
--     revealed=7 (ticket 5)  → A 賞大獎，is_designated_prize=1
--     revealed=1 (ticket 4)  → B 賞
--     revealed=5 (ticket 6)  → B 賞
--     revealed=8 (ticket 1)  → B 賞
--     revealed=2,4,6,9,10    → 謝謝惠顧（prize_id=NULL）
-- ================================================================

SET @ss_lottery = 'f0a00001-0000-4000-a000-000000000001';
SET @ss_prize_A = 'f0a00001-0001-4000-a000-000000000001';
SET @ss_prize_B = 'f0a00001-0002-4000-a000-000000000001';
SET @ss_t1  = 'f0a00001-0101-4000-a000-000000000001';
SET @ss_t2  = 'f0a00001-0102-4000-a000-000000000001';
SET @ss_t3  = 'f0a00001-0103-4000-a000-000000000001';
SET @ss_t4  = 'f0a00001-0104-4000-a000-000000000001';
SET @ss_t5  = 'f0a00001-0105-4000-a000-000000000001';
SET @ss_t6  = 'f0a00001-0106-4000-a000-000000000001';
SET @ss_t7  = 'f0a00001-0107-4000-a000-000000000001';
SET @ss_t8  = 'f0a00001-0108-4000-a000-000000000001';
SET @ss_t9  = 'f0a00001-0109-4000-a000-000000000001';
SET @ss_t10 = 'f0a00001-010a-4000-a000-000000000001';

-- ── lottery ──
INSERT INTO lottery (
    id, store_id, title, category,
    play_mode, game_mode,
    price_per_draw, max_draws,
    protection_draws, protection_minutes, free_draw_enabled,
    designated_prize_numbers,
    tickets_generated, status, order_num,
    created_at, updated_at
) VALUES (
    @ss_lottery, @store_id,
    '【SCRATCH_STORE】鬼滅之刃 刮刮樂 10 抽',
    'SCRATCH',
    'SCRATCH_MODE', 'SCRATCH_STORE',
    100, 10,
    3, 5, 1,    -- 保護：3 抽 / 5 分鐘 / 免單開啟
    '[3, 7]',   -- revealed_number 3 和 7 是大獎
    1,          -- tickets_generated=1（下方手動插入完整籤位）
    'ON_SHELF', 1,
    NOW(), NOW()
);

-- ── 獎品 ──
INSERT INTO lottery_prize (id, lottery_id, name, level, quantity, remaining, weight, is_grand_prize, is_last_prize, order_num, created_at, updated_at) VALUES
(@ss_prize_A, @ss_lottery, 'A 賞 炭治郎水晶公仔（大獎）', 'A', 2, 2, 100, 1, 0, 1, NOW(), NOW()),
(@ss_prize_B, @ss_lottery, 'B 賞 禰豆子徽章組',           'B', 3, 3, 50,  0, 0, 2, NOW(), NOW());

-- ── lottery_ticket（10 筆，含 revealed_number）──
-- 欄位：id, lottery_id, ticket_number, revealed_number, prize_id, prize_level,
--       status, is_designated_prize, designated_by, created_at, updated_at
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, revealed_number, prize_id, prize_level, status, is_designated_prize, designated_by, created_at, updated_at) VALUES
-- ticket 1: revealed=8 → B 賞
(@ss_t1,  @ss_lottery, 1,  8,  @ss_prize_B, 'B',      'AVAILABLE', 0, NULL,    NOW(), NOW()),
-- ticket 2: revealed=3 → A 賞大獎！
(@ss_t2,  @ss_lottery, 2,  3,  @ss_prize_A, 'A',      'AVAILABLE', 1, 'STORE', NOW(), NOW()),
-- ticket 3: revealed=10 → 謝謝惠顧
(@ss_t3,  @ss_lottery, 3,  10, NULL,        'THANKS',  'AVAILABLE', 0, NULL,    NOW(), NOW()),
-- ticket 4: revealed=1 → B 賞
(@ss_t4,  @ss_lottery, 4,  1,  @ss_prize_B, 'B',      'AVAILABLE', 0, NULL,    NOW(), NOW()),
-- ticket 5: revealed=7 → A 賞大獎！
(@ss_t5,  @ss_lottery, 5,  7,  @ss_prize_A, 'A',      'AVAILABLE', 1, 'STORE', NOW(), NOW()),
-- ticket 6: revealed=5 → B 賞
(@ss_t6,  @ss_lottery, 6,  5,  @ss_prize_B, 'B',      'AVAILABLE', 0, NULL,    NOW(), NOW()),
-- ticket 7: revealed=2 → 謝謝惠顧
(@ss_t7,  @ss_lottery, 7,  2,  NULL,        'THANKS',  'AVAILABLE', 0, NULL,    NOW(), NOW()),
-- ticket 8: revealed=9 → 謝謝惠顧
(@ss_t8,  @ss_lottery, 8,  9,  NULL,        'THANKS',  'AVAILABLE', 0, NULL,    NOW(), NOW()),
-- ticket 9: revealed=6 → 謝謝惠顧
(@ss_t9,  @ss_lottery, 9,  6,  NULL,        'THANKS',  'AVAILABLE', 0, NULL,    NOW(), NOW()),
-- ticket 10: revealed=4 → 謝謝惠顧
(@ss_t10, @ss_lottery, 10, 4,  NULL,        'THANKS',  'AVAILABLE', 0, NULL,    NOW(), NOW());


-- ================================================================
-- 範例二：SCRATCH_PLAYER（開套玩家指定大獎位置）
-- ================================================================
--
-- 10 張刮刮樂卡：
--   ticket_number → revealed_number shuffle 映射：
--   1→5、2→1、3→9、4→3、5→7、6→2、7→10、8→4、9→6、10→8
--
--   所有 prize_id = NULL，is_designated_prize = 0
--   開套玩家需從 availableNumbers=[5,1,9,3,7,2,10,4,6,8] 中選 2 個
--   指定哪些 revealedNumber 是 A 賞大獎（例：選 3 和 7）
--   後端 autoAssignNonGrandPrizes() 自動分配 B 賞給其餘 3 個
--   剩餘 5 個 → 謝謝惠顧
-- ================================================================

SET @sp_lottery = 'f0a00002-0000-4000-a000-000000000002';
SET @sp_prize_A = 'f0a00002-0001-4000-a000-000000000002';
SET @sp_prize_B = 'f0a00002-0002-4000-a000-000000000002';
SET @sp_t1  = 'f0a00002-0101-4000-a000-000000000002';
SET @sp_t2  = 'f0a00002-0102-4000-a000-000000000002';
SET @sp_t3  = 'f0a00002-0103-4000-a000-000000000002';
SET @sp_t4  = 'f0a00002-0104-4000-a000-000000000002';
SET @sp_t5  = 'f0a00002-0105-4000-a000-000000000002';
SET @sp_t6  = 'f0a00002-0106-4000-a000-000000000002';
SET @sp_t7  = 'f0a00002-0107-4000-a000-000000000002';
SET @sp_t8  = 'f0a00002-0108-4000-a000-000000000002';
SET @sp_t9  = 'f0a00002-0109-4000-a000-000000000002';
SET @sp_t10 = 'f0a00002-010a-4000-a000-000000000002';

-- ── lottery ──
INSERT INTO lottery (
    id, store_id, title, category,
    play_mode, game_mode,
    price_per_draw, max_draws,
    protection_draws, protection_minutes, free_draw_enabled,
    designated_prize_numbers,
    tickets_generated, status, order_num,
    created_at, updated_at
) VALUES (
    @sp_lottery, @store_id,
    '【SCRATCH_PLAYER】呪術廻戰 刮刮樂 10 抽',
    'SCRATCH',
    'SCRATCH_MODE', 'SCRATCH_PLAYER',
    150, 10,
    5, 10, 1,   -- 保護：5 抽 / 10 分鐘 / 免單開啟
    NULL,       -- 不預設大獎，等開套玩家呼叫 /designate
    1,          -- tickets_generated=1（手動插入）
    'ON_SHELF', 2,
    NOW(), NOW()
);

-- ── 獎品（is_grand_prize=1 才會出現在 /designate 回應的 grandPrizes 清單）──
INSERT INTO lottery_prize (id, lottery_id, name, level, quantity, remaining, weight, is_grand_prize, is_last_prize, order_num, created_at, updated_at) VALUES
(@sp_prize_A, @sp_lottery, 'A 賞 五條悟頭像掛墜（大獎）', 'A', 2, 2, 100, 1, 0, 1, NOW(), NOW()),
(@sp_prize_B, @sp_lottery, 'B 賞 虎杖悠仁貼紙組',         'B', 3, 3, 50,  0, 0, 2, NOW(), NOW());

-- ── lottery_ticket（SCRATCH_PLAYER 特有：prize_id 全 NULL）──
-- revealed_number 已 shuffle 完畢，但全部未指定大獎
INSERT INTO lottery_ticket (id, lottery_id, ticket_number, revealed_number, prize_id, prize_level, status, is_designated_prize, designated_by, created_at, updated_at) VALUES
(@sp_t1,  @sp_lottery, 1,  5,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t2,  @sp_lottery, 2,  1,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t3,  @sp_lottery, 3,  9,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t4,  @sp_lottery, 4,  3,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t5,  @sp_lottery, 5,  7,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t6,  @sp_lottery, 6,  2,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t7,  @sp_lottery, 7,  10, NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t8,  @sp_lottery, 8,  4,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t9,  @sp_lottery, 9,  6,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW()),
(@sp_t10, @sp_lottery, 10, 8,  NULL, NULL, 'AVAILABLE', 0, NULL, NOW(), NOW());


-- ================================================================
-- 驗證查詢
-- ================================================================

SELECT '── 商品列表 ──' AS '';
SELECT id, title, play_mode, game_mode, designated_prize_numbers, tickets_generated, status, protection_draws, protection_minutes
FROM lottery WHERE id IN (@ss_lottery, @sp_lottery);

SELECT '── SCRATCH_STORE 籤位（應有 revealed_number + prize_id）──' AS '';
SELECT ticket_number, revealed_number, prize_level, is_designated_prize, status
FROM lottery_ticket WHERE lottery_id = @ss_lottery ORDER BY ticket_number;

SELECT '── SCRATCH_PLAYER 籤位（prize_id 全 NULL，等開套玩家指定）──' AS '';
SELECT ticket_number, revealed_number, prize_id, is_designated_prize, status
FROM lottery_ticket WHERE lottery_id = @sp_lottery ORDER BY ticket_number;


-- ================================================================
-- 測試流程（Postman）
-- ================================================================

-- ── SCRATCH_STORE ──
-- 1. 查詢籤位（revealedNumber 全為 null，安全隱藏）
--    GET /api/lottery/draw/f0a00001-0000-4000-a000-000000000001/tickets
--
-- 2. 抽 ticket 2（revealed=3，大獎）
--    POST /api/lottery/draw/f0a00001-0000-4000-a000-000000000001/draw
--    { "count": 1, "ticket": ["f0a00001-0102-4000-a000-000000000001"] }
--
--    預期回應：
--    {
--      "data": { "playMode": "SCRATCH_MODE", "gameMode": "SCRATCH_STORE",
--        "results": [{ "ticketNumber": 2, "revealedNumber": 3,
--          "prizeLevel": "A", "isGrandPrize": true }] } }

-- ── SCRATCH_PLAYER ──
-- 1. 嘗試抽獎（被攔截，要求指定大獎）
--    POST /api/lottery/draw/f0a00002-0000-4000-a000-000000000002/draw
--    { "count": 1, "ticket": ["f0a00002-0101-4000-a000-000000000002"] }
--
--    預期攔截回應：
--    { "data": { "designationRequired": true,
--        "availableNumbers": [5,1,9,3,7,2,10,4,6,8],
--        "grandPrizes": [{ "prizeId": "f0a00002-0001-4000-a000-000000000002", "quantity": 2 }] } }
--
-- 2. 指定大獎（選 revealedNumber=3 和 7）
--    POST /api/lottery/draw/f0a00002-0000-4000-a000-000000000002/designate
--    { "designations": [
--        { "revealedNumber": 3, "prizeId": "f0a00002-0001-4000-a000-000000000002" },
--        { "revealedNumber": 7, "prizeId": "f0a00002-0001-4000-a000-000000000002" }
--    ] }
--
-- 3. 之後正常抽獎，抽 ticket 4（revealed=3，大獎）
--    POST /api/lottery/draw/f0a00002-0000-4000-a000-000000000002/draw
--    { "count": 1, "ticket": ["f0a00002-0104-4000-a000-000000000002"] }


-- ================================================================
-- 保護時間模式對照
-- ================================================================
--
-- ❌ 扭蛋（LOTTERY_MODE, category=GACHA）：
--    protection_draws = 0, protection_minutes = 0
--    原因：每次都是獨立隨機拉取，不需要排隊秩序
--
-- ✅ 一番賞（LOTTERY_MODE, category=ICHIBAN）：
--    protection_draws = 5, protection_minutes = 10（建議值）
--    原因：有完整獎品套組，第一位開套者需獨佔保護期
--
-- ✅ 刮刮樂（SCRATCH_MODE，SCRATCH_STORE/SCRATCH_PLAYER）：
--    保護時間必須設置（現實世界一套刮刮樂要依序刮）
--    小套（10-20 抽）→ protection_draws=3, protection_minutes=5
--    中套（30-50 抽）→ protection_draws=5, protection_minutes=10
--    大套（60-100 抽）→ protection_draws=10, protection_minutes=15
-- ================================================================
