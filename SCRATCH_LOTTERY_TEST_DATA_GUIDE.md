# 🎯 刮刮樂測試數據說明文檔

## 📋 概述

本文件提供刮刮樂功能的完整測試數據，包含 3 個不同規模的刮刮樂商品，適用於各種測試場景。

## 🎮 測試數據列表

### 1️⃣ 海賊王刮刮樂（小型）

**基本資訊：**
- **商品名稱：** 🏴‍☠️ 海賊王刮刮樂
- **總抽數：** 30 抽
- **每抽價格：** 50 元
- **折扣價格：** 40 元（大獎售完後）
- **獎品數量：** 20 個實體獎品 + 10 個謝謝惠顧

**獎品配置：**
| 等級 | 獎品名稱 | 數量 | 類型 | 是否大獎 |
|------|---------|------|------|---------|
| A | 魯夫 PVC 公仔（珍藏版） | 1 | FIGURE | ✅ |
| B | 索隆 三刀流模型組 | 2 | GOODS | - |
| C | 娜美 透明資料夾（5入） | 5 | GOODS | - |
| D | 海賊王角色徽章（隨機） | 7 | GOODS | - |
| E | 千陽號造型橡皮擦 | 5 | GOODS | - |

**特色：**
- ✅ 啟用自動降價
- ✅ 允許多抽（5抽、10抽）
- ✅ 紅利回饋：每抽 5 點
- 🎯 中獎率：66.7%（20/30）
- 🎁 謝謝惠顧率：33.3%（10/30）

**適用場景：**
- 測試基本刮刮樂功能
- 驗證謝謝惠顧機制
- 測試大獎售完後自動降價

---

### 2️⃣ 寶可夢刮刮樂（中型）

**基本資訊：**
- **商品名稱：** ⚡ 寶可夢刮刮樂
- **總抽數：** 50 抽
- **每抽價格：** 80 元
- **折扣價格：** 65 元（大獎售完後）
- **獎品數量：** 30 個實體獎品 + 20 個謝謝惠顧

**獎品配置：**
| 等級 | 獎品名稱 | 數量 | 類型 | 是否大獎 |
|------|---------|------|------|---------|
| A | 皮卡丘絨毛玩偶（30cm） | 2 | FIGURE | ✅ |
| B | 伊布進化系列盒玩（隨機） | 3 | FIGURE | - |
| C | 精靈球造型收納盒 | 5 | GOODS | - |
| D | 寶可夢卡牌擴充包（5張裝） | 10 | GOODS | - |
| E | 寶可夢造型貼紙（10入） | 10 | GOODS | - |

**特色：**
- ✅ 啟用自動降價
- ✅ 允許多抽（5抽、10抽、20抽）
- ✅ 紅利回饋：每抽 8 點
- 🎯 中獎率：60%（30/50）
- 🎁 謝謝惠顧率：40%（20/50）
- 🔥 熱度：38（高人氣）

**適用場景：**
- 測試中等規模刮刮樂
- 驗證多抽選項功能
- 測試較高紅利回饋

---

### 3️⃣ 鬼滅之刃刮刮樂（大型）

**基本資訊：**
- **商品名稱：** ⚔️ 鬼滅之刃刮刮樂
- **總抽數：** 100 抽
- **每抽價格：** 60 元
- **折扣價格：** 無（不啟用自動降價）
- **獎品數量：** 40 個實體獎品 + 60 個謝謝惠顧

**獎品配置：**
| 等級 | 獎品名稱 | 數量 | 類型 | 是否大獎 |
|------|---------|------|------|---------|
| A | 竈門炭治郎景品公仔（25cm） | 1 | FIGURE | ✅ |
| B | 竈門禰豆子 Q版模型 | 2 | FIGURE | - |
| C | 呼吸法特效配件組 | 3 | GOODS | - |
| D | 鬼滅角色壓克力立牌（隨機） | 10 | GOODS | - |
| E | 鬼滅和紙膠帶（3入組） | 12 | GOODS | - |
| F | 鬼滅角色徽章（隨機2入） | 12 | GOODS | - |

**特色：**
- ❌ 不啟用自動降價
- ✅ 允許多抽（5抽、10抽、20抽、50抽）
- ✅ 紅利回饋：每抽 6 點
- 🎯 中獎率：40%（40/100）
- 🎁 謝謝惠顧率：60%（60/100）
- 🔥 熱度：50（超高人氣）
- 📊 獎品種類最多（6種等級）

**適用場景：**
- 測試大規模刮刮樂
- 驗證高謝謝惠顧率
- 測試多種獎品等級
- 壓力測試

---

## 🚀 使用方式

### 方法 1：使用 BAT 腳本（推薦）

```bash
# 直接執行批次檔
run-scratch-lottery-data.bat
```

### 方法 2：手動執行 SQL

```bash
# 方式 A：命令列導入
mysql -u root -p kuji_db < create-scratch-lottery-data.sql

# 方式 B：MySQL Workbench
# 1. 開啟 MySQL Workbench
# 2. File → Run SQL Script
# 3. 選擇 create-scratch-lottery-data.sql
# 4. 執行
```

---

## 🔍 驗證方式

### 1. 查詢所有刮刮樂商品

```sql
SELECT 
    title AS '商品名稱',
    price_per_draw AS '每抽價格',
    max_draws AS '總抽數',
    total_draws AS '已抽次數',
    status AS '狀態',
    hot_count AS '熱度'
FROM lottery 
WHERE play_mode = 'SCRATCH_MODE'
ORDER BY order_num;
```

### 2. 查詢獎品統計

```sql
SELECT 
    l.title AS '商品名稱',
    COUNT(lp.id) AS '獎品種類',
    SUM(lp.quantity) AS '總獎品數',
    l.max_draws AS '總抽數',
    (l.max_draws - SUM(lp.quantity)) AS '謝謝惠顧數'
FROM lottery l
LEFT JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.play_mode = 'SCRATCH_MODE'
GROUP BY l.id, l.title, l.max_draws;
```

### 3. 查詢大獎資訊

```sql
SELECT 
    l.title AS '商品',
    lp.name AS '大獎名稱',
    lp.quantity AS '數量',
    lp.remaining AS '剩餘'
FROM lottery l
INNER JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.play_mode = 'SCRATCH_MODE' 
  AND lp.is_grand_prize = 1;
```

---

## 🎮 測試場景建議

### 場景 1：基本功能測試（使用海賊王刮刮樂）

1. **查看商品列表**
   - GET `/api/lottery/list`
   - 確認刮刮樂商品顯示正確

2. **查看商品詳情**
   - GET `/api/lottery/{lotteryId}`
   - 確認獎品資訊完整

3. **進行抽獎**
   - POST `/api/lottery/{lotteryId}/draw`
   - 測試單抽功能

4. **測試多抽**
   - POST `/api/lottery/{lotteryId}/draw-multiple`
   - Body: `{ "quantity": 5 }`

5. **驗證謝謝惠顧**
   - 多次抽獎後，應該會出現「謝謝惠顧」結果

### 場景 2：大獎售完後降價（使用海賊王刮刮樂）

1. 查詢 A 賞的 prize_id
2. 手動標記為已抽中：
   ```sql
   UPDATE lottery_prize 
   SET remaining = 0 
   WHERE lottery_id = '...' AND level = 'A';
   ```
3. 重新查詢商品價格，應從 50 元變為 40 元

### 場景 3：大規模測試（使用鬼滅之刃刮刮樂）

1. 測試連續抽獎 50 次
2. 統計中獎率是否接近 40%
3. 驗證獎品扣減是否正確
4. 測試 50 抽一次的多抽功能

### 場景 4：後台管理測試

1. **查詢刮刮樂商品**
   - GET `/admin/lottery/list`
   - Body: `{ "condition": { "playMode": "SCRATCH_MODE" } }`

2. **修改商品狀態**
   - PUT `/admin/lottery/{lotteryId}`
   - 測試上架/下架功能

3. **查詢抽獎記錄**
   - GET `/admin/lottery/{lotteryId}/draw-records`

---

## 📊 預期結果

### 導入成功後應該看到：

```
✅ 刮刮樂測試數據創建完成！
📊 總共創建了 3 個刮刮樂商品
🎁 總共創建了 17 個獎品項目

=== 刮刮樂商品列表 ===
+------+------------------------+---------+
| 商品名稱                  | 總抽數  | 狀態    |
+------+------------------------+---------+
| 海賊王刮刮樂              | 30      | ON_SHELF|
| 寶可夢刮刮樂              | 50      | ON_SHELF|
| 鬼滅之刃刮刮樂            | 100     | ON_SHELF|
+------+------------------------+---------+

=== 獎品統計 ===
+------------------------+----------+----------+------------+
| 商品名稱              | 獎品數   | 總抽數   | 謝謝惠顧   |
+------------------------+----------+----------+------------+
| 海賊王刮刮樂          | 20       | 30       | 10         |
| 寶可夢刮刮樂          | 30       | 50       | 20         |
| 鬼滅之刃刮刮樂        | 40       | 100      | 60         |
+------------------------+----------+----------+------------+
```

---

## ⚠️ 注意事項

### 前置條件

1. **店家數據必須存在**
   ```sql
   SELECT id, store_name FROM store;
   ```
   如果沒有店家，請先創建：
   ```sql
   INSERT INTO store (id, store_name, ...) VALUES (...);
   ```

2. **管理員用戶必須存在**
   ```sql
   SELECT id FROM admin_user WHERE username = 'admin@kuji.com';
   ```

### 修改店家 ID

如果需要指定特定店家，修改 SQL 文件第 16 行：
```sql
SET @store_id_1 = 'your-store-id-here';
```

### 清理測試數據

如果需要重新導入，先清理舊數據：
```sql
-- 刪除刮刮樂獎品
DELETE FROM lottery_prize 
WHERE lottery_id IN (
    SELECT id FROM lottery WHERE play_mode = 'SCRATCH_MODE'
);

-- 刪除刮刮樂商品
DELETE FROM lottery WHERE play_mode = 'SCRATCH_MODE';
```

---

## 🆘 常見問題

### Q1: 導入後找不到數據？

**A:** 檢查數據是否真的導入成功：
```sql
SELECT COUNT(*) FROM lottery WHERE play_mode = 'SCRATCH_MODE';
```

### Q2: 獎品數量不對？

**A:** 驗證獎品表：
```sql
SELECT 
    l.title,
    lp.level,
    lp.quantity,
    lp.remaining
FROM lottery l
INNER JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.play_mode = 'SCRATCH_MODE';
```

### Q3: 前端顯示不出來？

**A:** 檢查商品狀態：
```sql
UPDATE lottery 
SET status = 'ON_SHELF' 
WHERE play_mode = 'SCRATCH_MODE';
```

### Q4: 抽獎時出現錯誤？

**A:** 檢查 lottery_ticket 表是否已生成籤位：
```sql
SELECT COUNT(*) 
FROM lottery_ticket 
WHERE lottery_id IN (
    SELECT id FROM lottery WHERE play_mode = 'SCRATCH_MODE'
);
```

如果沒有籤位，需要觸發籤位生成機制（通常在商品上架時自動生成）。

---

## 📞 技術支援

如有問題，請提供：
1. 完整錯誤訊息
2. 執行的 SQL 語句
3. 資料庫版本（`SELECT VERSION();`）
4. 相關表的數據狀態

---

## ✅ 測試檢查清單

- [ ] 數據導入成功
- [ ] 可查詢到 3 個刮刮樂商品
- [ ] 獎品數量正確
- [ ] 商品狀態為 ON_SHELF
- [ ] 可正常進行單抽
- [ ] 可正常進行多抽
- [ ] 會出現「謝謝惠顧」
- [ ] 大獎售完後自動降價（海賊王、寶可夢）
- [ ] 紅利回饋正確

---

## 📝 更新記錄

- **2026-02-11**：初版創建
  - 新增 3 個刮刮樂測試商品
  - 包含完整獎品配置
  - 提供 BAT 執行腳本
  - 撰寫完整說明文檔
