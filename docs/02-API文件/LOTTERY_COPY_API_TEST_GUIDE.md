# 複製商品功能 API 測試指南

## 功能說明

複製商品功能可以將指定的一番賞或刮刮樂商品**完整複製**，包含：
- ✅ Lottery 主表資料（產生新 ID、更新標題）
- ✅ 所有 LotteryPrize（獎項）
- ✅ 可選擇是否重新生成籤號

---

## API 資訊

### 基本資訊

- **路徑**：`POST /api/admin/lottery/copy`
- **權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`
- **Content-Type**：`application/json`
- **需要 Token**：✅ 是

---

## 請求格式

### Request Body

```json
{
  "sourceLotteryId": "uuid-string",        // 必填：要複製的來源商品 ID
  "newTitle": "鬼滅之刃一番賞（複製）",      // 選填：新商品標題（若為空則自動加上「複製」）
  "regenerateTickets": true,               // 選填：是否重新生成籤號（預設 true）
  "newStatus": "OFF_SHELF"                 // 選填：新商品狀態（預設 OFF_SHELF）
}
```

### 欄位說明

| 欄位 | 類型 | 必填 | 說明 | 範例 |
|------|------|------|------|------|
| `sourceLotteryId` | String | ✅ 是 | 要複製的來源商品 ID（UUID 格式） | `"123e4567-e89b-12d3-a456-426614174000"` |
| `newTitle` | String | ❌ 否 | 新商品標題。若為空，自動加上「（複製）」後綴 | `"鬼滅之刃一番賞（複製）"` |
| `regenerateTickets` | Boolean | ❌ 否 | 是否重新生成籤號。預設 `true` | `true` / `false` |
| `newStatus` | String | ❌ 否 | 新商品狀態。預設 `OFF_SHELF`，避免立即上架 | `"ON_SHELF"` / `"OFF_SHELF"` / `"SOLD_OUT"` |

---

## 複製規則

### 1. Lottery 主表

| 欄位 | 複製規則 | 說明 |
|------|----------|------|
| `id` | ✅ 產生新 UUID | 新商品 ID |
| `storeId` | ✅ 相同 | 保持原店家 |
| `title` | ⚠️ 依參數 | 若 `newTitle` 為空，自動加上「（複製）」 |
| `imageUrl` | ✅ 相同 | 保持原圖片 |
| `category` | ✅ 相同 | 保持原分類 |
| `subCategory` | ✅ 相同 | 保持原子分類 |
| `gameMode` | ✅ 相同 | 保持原遊戲模式 |
| `pricePerDraw` | ✅ 相同 | 保持原價格 |
| `discountedPrice` | ✅ 相同 | 保持原折扣價 |
| `autoDiscountEnabled` | ✅ 相同 | 保持原設定 |
| `allowMultiDraw` | ✅ 相同 | 保持原設定 |
| `multiDrawOptions` | ✅ 相同 | 保持原設定 |
| `scheduledAt` | ✅ 相同 | 保持原排程時間 |
| `startTime` | ✅ 相同 | 保持原開始時間 |
| `endTime` | ✅ 相同 | 保持原結束時間 |
| `totalDraws` | ⚠️ 重置為 0 | 重新計算 |
| `maxDraws` | ✅ 相同 | 保持原上限 |
| `protectionDraws` | ✅ 相同 | 保持原保底設定 |
| `protectionMinutes` | ✅ 相同 | 保持原保底時間 |
| `freeDrawEnabled` | ✅ 相同 | 保持原設定 |
| `designatedPrizeNumbers` | ✅ 相同 | 保持原指定大獎號碼 |
| `ticketsGenerated` | ⚠️ 依參數 | 若 `regenerateTickets=true` 則重置為 0 |
| `status` | ⚠️ 依參數 | 預設 `OFF_SHELF`，避免立即上架 |
| `orderNum` | ✅ 相同 | 保持原排序 |
| `weight` | ✅ 相同 | 保持原權重 |
| `createdBy` | ✅ 相同 | 保持原建立者 |
| `createdAt` | ⚠️ 現在時間 | 新建立時間 |
| `updatedAt` | ⚠️ 現在時間 | 新更新時間 |
| `description` | ✅ 相同 | 保持原描述 |
| `remark` | ⚠️ 標註來源 | `"複製自商品：{原標題}"` |

### 2. LotteryPrize（獎項）

| 欄位 | 複製規則 | 說明 |
|------|----------|------|
| `id` | ✅ 產生新 UUID | 新獎項 ID |
| `lotteryId` | ✅ 新商品 ID | 關聯到新商品 |
| `name` | ✅ 相同 | 保持原名稱 |
| `imageUrl` | ✅ 相同 | 保持原圖片 |
| `level` | ✅ 相同 | 保持原等級 |
| `prizeNumber` | ✅ 相同 | 保持原號碼 |
| `quantity` | ✅ 相同 | 保持原數量 |
| `remaining` | ⚠️ 重置為原數量 | 重新計算 |
| `weight` | ✅ 相同 | 保持原權重 |
| `prizeType` | ✅ 相同 | 保持原類型 |
| `pointValue` | ✅ 相同 | 保持原點數價值 |
| `isLastPrize` | ✅ 相同 | 保持原設定 |
| `isGrandPrize` | ✅ 相同 | 保持原設定 |
| `orderNum` | ✅ 相同 | 保持原排序 |
| `createdAt` | ⚠️ 現在時間 | 新建立時間 |
| `updatedAt` | ⚠️ 現在時間 | 新更新時間 |
| `description` | ✅ 相同 | 保持原描述 |

---

## 使用場景

### 場景 1：完整複製（預設）

最常見的使用方式，快速複製一個商品：

```json
{
  "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000"
}
```

**結果**：
- ✅ 標題自動加上「（複製）」
- ✅ 狀態為 `OFF_SHELF`
- ✅ 重新生成籤號
- ✅ 所有獎項完整複製

---

### 場景 2：指定新標題

複製並指定新標題：

```json
{
  "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000",
  "newTitle": "鬼滅之刃一番賞 2024 版"
}
```

**結果**：
- ✅ 使用指定的新標題
- ✅ 其他規則同場景 1

---

### 場景 3：保留籤號（不重新生成）

複製商品但保留原籤號配置：

```json
{
  "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000",
  "newTitle": "鬼滅之刃一番賞（複製）",
  "regenerateTickets": false
}
```

**結果**：
- ✅ 不重新生成籤號
- ⚠️ 注意：如果原商品沒有籤號，新商品也不會有

---

### 場景 4：複製後立即上架

複製商品並直接設為上架狀態：

```json
{
  "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000",
  "newTitle": "鬼滅之刃一番賞（熱銷再版）",
  "regenerateTickets": true,
  "newStatus": "ON_SHELF"
}
```

**結果**：
- ✅ 新商品狀態為 `ON_SHELF`
- ⚠️ 注意：確保所有設定正確後再上架

---

## 測試步驟

### 步驟 1：取得 Admin Token

```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@kuji.com",
    "password": "admin123"
  }'
```

**取得 Token**：
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": { ... }
  }
}
```

---

### 步驟 2：查詢現有商品（取得來源 ID）

```bash
curl -X POST http://localhost:8080/api/admin/lottery/list \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {YOUR_TOKEN}" \
  -d '{
    "condition": {
      "category": "OFFICIAL_ICHIBAN"
    }
  }'
```

**取得商品 ID**：
```json
{
  "success": true,
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "title": "鬼滅之刃一番賞",
      "category": "OFFICIAL_ICHIBAN",
      ...
    }
  ]
}
```

---

### 步驟 3：複製商品（完整複製）

```bash
curl -X POST http://localhost:8080/api/admin/lottery/copy \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {YOUR_TOKEN}" \
  -d '{
    "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

**成功回應（200 OK）**：
```json
{
  "success": true,
  "data": {
    "id": "新的UUID",
    "storeId": "原店家ID",
    "title": "鬼滅之刃一番賞（複製）",
    "imageUrl": "原圖片URL",
    "category": "OFFICIAL_ICHIBAN",
    "categoryName": "官方一番賞",
    "pricePerDraw": 80,
    "status": "OFF_SHELF",
    "statusName": "已下架",
    "totalDraws": 0,
    "totalPrizes": 100,
    "remainingPrizes": 100,
    "createdAt": "2026-01-09T21:00:00",
    "remark": "複製自商品：鬼滅之刃一番賞"
  },
  "meta": {
    "timestamp": "2026-01-09T21:00:00.123+08:00",
    "requestId": "uuid"
  }
}
```

---

### 步驟 4：驗證複製結果

#### 4.1 查詢新商品詳情

```bash
curl -X GET http://localhost:8080/api/admin/lottery/{新商品ID} \
  -H "Authorization: Bearer {YOUR_TOKEN}"
```

#### 4.2 查詢新商品的獎項

```sql
-- 查詢新商品的獎項
SELECT * FROM lottery_prize WHERE lottery_id = '新商品ID';

-- 對比原商品的獎項
SELECT * FROM lottery_prize WHERE lottery_id = '原商品ID';
```

#### 4.3 驗證數量一致

```sql
-- 新商品獎項數量
SELECT COUNT(*) FROM lottery_prize WHERE lottery_id = '新商品ID';

-- 原商品獎項數量
SELECT COUNT(*) FROM lottery_prize WHERE lottery_id = '原商品ID';
```

---

## 錯誤處理

### 錯誤 1：來源商品不存在

**請求**：
```json
{
  "sourceLotteryId": "不存在的UUID"
}
```

**回應（404）**：
```json
{
  "success": false,
  "error": {
    "code": "LOTTERY_NOT_FOUND",
    "message": "來源商品不存在"
  }
}
```

---

### 錯誤 2：權限不足

**回應（403）**：
```json
{
  "success": false,
  "error": {
    "code": "ACCESS_DENIED",
    "message": "權限不足"
  }
}
```

**解決**：
- 確認 Token 是否有效
- 確認角色是否為 `ROLE_ADMIN`, `ROLE_STORE_OWNER`, 或 `ROLE_STORE_EDITOR`

---

### 錯誤 3：驗證失敗

**請求**：
```json
{
  "sourceLotteryId": ""  // 空值
}
```

**回應（400）**：
```json
{
  "success": false,
  "error": {
    "code": "COMMON_VALIDATION_001",
    "message": "來源商品 ID 不可為空"
  }
}
```

---

## Postman Collection

### 請求設定

1. **Method**: `POST`
2. **URL**: `{{baseUrl}}/admin/lottery/copy`
3. **Headers**:
   ```
   Content-Type: application/json
   Authorization: Bearer {{adminToken}}
   ```
4. **Body (raw JSON)**:
   ```json
   {
     "sourceLotteryId": "{{sourceLotteryId}}"
   }
   ```

### 環境變數

```json
{
  "baseUrl": "http://localhost:8080/api",
  "adminToken": "your_admin_token_here",
  "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000"
}
```

---

## 完整範例（cURL）

### 範例 1：最簡單的複製

```bash
curl -X POST http://localhost:8080/api/admin/lottery/copy \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000"
  }' \
  -v
```

---

### 範例 2：完整參數

```bash
curl -X POST http://localhost:8080/api/admin/lottery/copy \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000",
    "newTitle": "鬼滅之刃一番賞 2024 熱銷再版",
    "regenerateTickets": true,
    "newStatus": "OFF_SHELF"
  }' \
  -v
```

---

## 注意事項

### ⚠️ 重要提醒

1. **新商品預設下架**
   - 預設狀態為 `OFF_SHELF`，避免立即上架
   - 建議複製後先檢查所有設定，確認無誤後再上架

2. **抽數統計重置**
   - `totalDraws` 重置為 0
   - `remainingDraws` 重新計算

3. **獎項數量重置**
   - 所有獎項的 `remaining` 重置為原始 `quantity`
   - 確保新商品是全新的狀態

4. **籤號生成**
   - 如果 `regenerateTickets=true`，需要手動觸發籤號生成
   - 如果 `regenerateTickets=false`，保留原籤號配置

5. **店家權限**
   - 只能複製自己店家的商品
   - Admin 可以複製所有店家的商品

---

## 常見問題

### Q1: 複製後可以修改嗎？

**A**: 可以。複製後的商品是全新的商品，可以使用更新 API 進行修改。

---

### Q2: 可以跨店家複製嗎？

**A**: 不可以。複製的新商品會保持原店家 ID，確保店家資料隔離。

---

### Q3: 複製後需要重新設定獎項嗎？

**A**: 不需要。所有獎項會自動複製，包括名稱、圖片、數量、權重等。

---

### Q4: 可以複製已上架的商品嗎？

**A**: 可以。來源商品的狀態不影響複製功能，新商品預設為 `OFF_SHELF`。

---

### Q5: 複製後籤號會自動生成嗎？

**A**: 不會自動生成。需要手動觸發籤號生成，或使用其他 API。

---

## 資料庫檢查

### 檢查新商品

```sql
-- 查詢新商品
SELECT * FROM lottery WHERE remark LIKE '複製自商品：%' ORDER BY created_at DESC LIMIT 1;
```

### 檢查新獎項

```sql
-- 查詢新商品的獎項
SELECT 
    lp.id,
    lp.lottery_id,
    lp.name,
    lp.level,
    lp.quantity,
    lp.remaining,
    lp.created_at
FROM lottery_prize lp
JOIN lottery l ON lp.lottery_id = l.id
WHERE l.remark LIKE '複製自商品：%'
ORDER BY lp.created_at DESC;
```

### 對比原商品與新商品

```sql
-- 對比獎項數量
SELECT 
    l.id,
    l.title,
    COUNT(lp.id) as prize_count,
    SUM(lp.quantity) as total_quantity,
    SUM(lp.remaining) as total_remaining
FROM lottery l
LEFT JOIN lottery_prize lp ON l.id = lp.lottery_id
WHERE l.id IN ('原商品ID', '新商品ID')
GROUP BY l.id, l.title;
```

---

**更新時間**：2026-01-09  
**API 版本**：v1.0  
**狀態**：✅ 已完成測試
