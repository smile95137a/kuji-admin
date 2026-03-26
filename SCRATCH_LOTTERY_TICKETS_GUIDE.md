# 🎫 刮刮樂籤位補充說明

## 問題分析

您的刮刮樂商品缺少 `lottery_ticket` 表的記錄，導致玩家無法選擇號碼進行抽獎。

## 刮刮樂籤位邏輯

### 籤位生成規則（參考 `LotteryTicketServiceImpl.generateScratchTickets()`）

1. **總籤位數 = `maxDraws`**
   - 每個籤位有唯一的 `ticket_number`（從 1 開始）
   
2. **獎品分配**
   - 從 `lottery_prize` 表讀取所有獎品
   - 根據每個獎品的 `quantity`，生成對應數量的籤位
   - 例如：A 賞 1 個 + B 賞 2 個 = 3 個獎品籤位

3. **謝謝惠顧**
   - 剩餘的籤位 = `maxDraws` - 獎品總數
   - 這些籤位的 `prize_id` 為 NULL，`prize_level` 為 "THANKS"

4. **隨機分配**
   - 所有籤位生成後，使用 `RAND()` 隨機打亂順序
   - 確保玩家無法預測哪個號碼是獎品

## 您的商品資訊

根據您提供的 ID：

| 商品名稱 | 商品 ID | 預期籤位數 |
|---------|---------|----------|
| 🏴‍☠️ 海賊王刮刮樂 | `638aa8ce-075c-11f1-bab7-0a7ddf3d3fc1` | 30 |
| ⚡ 寶可夢刮刮樂 | `63a02abc-075c-11f1-bab7-0a7ddf3d3fc1` | 50 |
| ⚔️ 鬼滅之刃刮刮樂 | `63b4278f-075c-11f1-bab7-0a7ddf3d3fc1` | 100 |

## 使用方式

### 方案一：為已存在的商品補籤位（推薦）

```bash
# 執行補籤位腳本
mysql -u root -p kuji_db < add-scratch-lottery-tickets.sql
```

**此腳本會**：
1. ✅ 檢查商品是否存在
2. ✅ 檢查獎品配置
3. ✅ 刪除舊籤位（如果有）
4. ✅ 為每個獎品等級生成對應數量的籤位
5. ✅ 補齊「謝謝惠顧」籤位
6. ✅ 隨機打亂順序
7. ✅ 驗證生成結果

### 方案二：創建全新的測試商品

```bash
# 執行測試數據腳本（會創建新商品 + 獎品 + 籤位）
mysql -u root -p kuji_db < create-scratch-lottery-data.sql
```

## 籤位表結構

```sql
CREATE TABLE lottery_ticket (
    id VARCHAR(36) PRIMARY KEY,           -- 籤位 ID
    lottery_id VARCHAR(36) NOT NULL,      -- 所屬商品 ID
    ticket_number INT NOT NULL,           -- 籤號（1, 2, 3...）
    prize_id VARCHAR(36),                 -- 獎品 ID（NULL = 謝謝惠顧）
    prize_level VARCHAR(20),              -- 獎品等級（A/B/C/THANKS）
    status VARCHAR(20),                   -- AVAILABLE/DRAWN/LOCKED
    drawn_by VARCHAR(36),                 -- 抽取者 ID
    drawn_at DATETIME,                    -- 抽取時間
    is_designated_prize TINYINT,          -- 是否為指定大獎
    designated_by VARCHAR(20),            -- 指定者類型
    created_at DATETIME,
    updated_at DATETIME
);
```

## 驗證籤位生成

執行以下查詢檢查籤位：

```sql
-- 檢查籤位數量
SELECT 
    l.title AS '商品名稱',
    l.max_draws AS '預期總數',
    COUNT(lt.id) AS '實際籤位數',
    SUM(CASE WHEN lt.prize_level != 'THANKS' THEN 1 ELSE 0 END) AS '獎品籤位',
    SUM(CASE WHEN lt.prize_level = 'THANKS' THEN 1 ELSE 0 END) AS '謝謝惠顧'
FROM lottery l
LEFT JOIN lottery_ticket lt ON l.id = lt.lottery_id
WHERE l.id IN (
    '638aa8ce-075c-11f1-bab7-0a7ddf3d3fc1',
    '63a02abc-075c-11f1-bab7-0a7ddf3d3fc1',
    '63b4278f-075c-11f1-bab7-0a7ddf3d3fc1'
)
GROUP BY l.id, l.title, l.max_draws;

-- 查看前 10 個籤位
SELECT 
    l.title,
    lt.ticket_number,
    lt.prize_level,
    CASE 
        WHEN lt.prize_level = 'THANKS' THEN '謝謝惠顧'
        ELSE lp.name
    END AS prize_name,
    lt.status
FROM lottery l
INNER JOIN lottery_ticket lt ON l.id = lt.lottery_id
LEFT JOIN lottery_prize lp ON lt.prize_id = lp.id
WHERE l.id = '638aa8ce-075c-11f1-bab7-0a7ddf3d3fc1'
ORDER BY lt.ticket_number
LIMIT 10;
```

## 範例數據結構

### 海賊王刮刮樂（30 抽）

| 籤號 | 獎品等級 | 獎品名稱 | 狀態 |
|-----|---------|---------|-----|
| 1 | C | 娜美透明資料夾 | AVAILABLE |
| 2 | THANKS | 謝謝惠顧 | AVAILABLE |
| 3 | D | 海賊王徽章 | AVAILABLE |
| 4 | B | 索隆武士刀模型 | AVAILABLE |
| 5 | THANKS | 謝謝惠顧 | AVAILABLE |
| ... | ... | ... | ... |
| 30 | A | 魯夫 PVC 公仔 | AVAILABLE |

**說明**：
- 總共 30 個籤位
- 20 個獎品籤位（A×1 + B×2 + C×5 + D×7 + E×5）
- 10 個謝謝惠顧
- 順序是隨機的（使用 `RAND()` 打亂）

## 前台 API 使用

生成籤位後，前台可以呼叫以下 API：

```http
### 查詢可選號碼
GET /api/lottery/draw/{lotteryId}/tickets
Authorization: Bearer {user_token}

### 指定號碼抽獎
POST /api/lottery/draw/{lotteryId}
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "count": 1,
  "tickets": [5, 12, 23]  // 指定要抽的籤號
}
```

## 注意事項

1. **籤位必須在商品上架前生成**
   - 後台呼叫 `createLotteryWithPrizes` API 時會自動生成
   - 如果手動插入商品，需要執行補籤位腳本

2. **籤位生成後不應修改**
   - 獎品分配是隨機的，修改會影響公平性
   - 如需重新生成，先 DELETE 再執行腳本

3. **前台 API 不會洩漏未抽籤位的獎品資訊**
   - 狀態為 AVAILABLE 的籤位，前台只返回 `ticket_number` 和 `status`
   - 只有抽中後才會返回 `prize_level` 和 `prize_name`

4. **刮刮樂必須有 playMode = 'SCRATCH_MODE'**
   - 這樣系統才會正確生成籤位
   - 不同的模式有不同的籤位生成邏輯

## 相關文件

- [LOTTERY_TICKET_IMPLEMENTATION.md](./doc/LOTTERY_TICKET_IMPLEMENTATION.md) - 籤位系統完整實作指南
- [LotteryTicketServiceImpl.java](./src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java) - 籤位服務實作
- [DDL_lottery_ticket_system.sql](./doc/DDL_lottery_ticket_system.sql) - 籤位表結構

## 問題排查

### 問題：籤位數量不符

```sql
-- 檢查 lottery 表的 max_draws
SELECT id, title, max_draws FROM lottery WHERE id = 'xxx';

-- 檢查 lottery_prize 的總數量
SELECT SUM(quantity) FROM lottery_prize WHERE lottery_id = 'xxx';

-- 預期結果：max_draws = 獎品總數 + 謝謝惠顧數
```

### 問題：前台看不到籤位

```sql
-- 檢查籤位狀態
SELECT COUNT(*), status FROM lottery_ticket 
WHERE lottery_id = 'xxx' 
GROUP BY status;

-- 應該都是 AVAILABLE
```

### 問題：籤位順序不隨機

```sql
-- 重新打亂順序
SET @counter := 0;
UPDATE lottery_ticket lt
LEFT JOIN (
    SELECT id, (@counter := @counter + 1) AS new_number
    FROM lottery_ticket
    WHERE lottery_id = 'xxx'
    ORDER BY RAND()
) shuffled ON lt.id = shuffled.id
SET lt.ticket_number = shuffled.new_number
WHERE lt.lottery_id = 'xxx';
```

## 完成確認

執行腳本後，您應該看到：

```
✅ 海賊王刮刮樂籤位生成完成：30 個籤位
✅ 寶可夢刮刮樂籤位生成完成：50 個籤位
✅ 鬼滅之刃刮刮樂籤位生成完成：100 個籤位
✅ 所有籤位生成完成！
```

然後可以在前台測試選號抽獎功能了！🎉
