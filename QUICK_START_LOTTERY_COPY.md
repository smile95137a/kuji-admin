# 複製商品功能 - 快速使用指南

## 🎯 功能說明

前端工程師需要一個 API 可以將指定的商品（一番賞、刮刮樂）**完整複製**出來。

---

## ✅ 已完成實作

- ✅ 複製 Lottery 主表資料（產生新 ID）
- ✅ 複製所有 LotteryPrize（獎項）
- ✅ 自動重置抽數統計
- ✅ 自動重置獎項剩餘數量
- ✅ 支援自訂新標題
- ✅ 支援選擇是否重新生成籤號
- ✅ 支援自訂新商品狀態

---

## 📌 API 端點

```
POST /api/admin/lottery/copy
```

**權限**：`ROLE_ADMIN`, `ROLE_STORE_OWNER`, `ROLE_STORE_EDITOR`

---

## 🚀 快速使用（前端）

### 最簡化使用

```typescript
// 只傳來源 ID，其他自動處理
const response = await axios.post('/api/admin/lottery/copy', {
  sourceLotteryId: '123e4567-e89b-12d3-a456-426614174000'
});

// 結果：
// - 標題自動加上「（複製）」
// - 狀態為 OFF_SHELF
// - 抽數重置為 0
// - 所有獎項完整複製
```

### 完整參數

```typescript
const response = await axios.post('/api/admin/lottery/copy', {
  sourceLotteryId: '123e4567-e89b-12d3-a456-426614174000',
  newTitle: '鬼滅之刃一番賞 2024 版',       // 選填
  regenerateTickets: true,                   // 選填（預設 true）
  newStatus: 'OFF_SHELF'                     // 選填（預設 OFF_SHELF）
});
```

---

## 📝 請求格式

### Request Body（最簡化）

```json
{
  "sourceLotteryId": "uuid-string"
}
```

### Request Body（完整參數）

```json
{
  "sourceLotteryId": "uuid-string",        // 必填：來源商品 ID
  "newTitle": "新標題",                     // 選填：不填則自動加上「（複製）」
  "regenerateTickets": true,               // 選填：預設 true
  "newStatus": "OFF_SHELF"                 // 選填：預設 OFF_SHELF
}
```

---

## ✅ 成功回應（200 OK）

```json
{
  "success": true,
  "data": {
    "id": "新的UUID",
    "storeId": "原店家ID",
    "storeName": "店家名稱",
    "title": "鬼滅之刃一番賞（複製）",
    "imageUrl": "原圖片URL",
    "category": "OFFICIAL_ICHIBAN",
    "categoryName": "官方一番賞",
    "pricePerDraw": 80,
    "status": "OFF_SHELF",
    "statusName": "已下架",
    "totalDraws": 0,                      // ✅ 重置為 0
    "totalPrizes": 50,
    "remainingPrizes": 50,                // ✅ 重置為原始數量
    "createdAt": "2026-01-09T21:00:00",
    "remark": "複製自商品：鬼滅之刃一番賞"
  }
}
```

---

## ❌ 錯誤回應

### 1. 來源商品不存在（404）

```json
{
  "success": false,
  "error": {
    "code": "LOTTERY_NOT_FOUND",
    "message": "來源商品不存在"
  }
}
```

### 2. 權限不足（403）

```json
{
  "success": false,
  "error": {
    "code": "ACCESS_DENIED",
    "message": "權限不足"
  }
}
```

### 3. 驗證失敗（400）

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

## 🧪 測試方式

### 方法 1：使用 Postman（推薦）

1. 匯入 Collection：`KUJI_Lottery_Copy_API.postman_collection.json`
2. 執行測試（按順序）：
   - ① 登入取得 Token
   - ② 查詢商品列表（取得來源 ID）
   - ③ 複製商品（最簡化）
   - ④ 查詢新商品詳情

### 方法 2：使用測試腳本

```bash
# Windows
.\test-lottery-copy.bat

# 按照提示輸入：
# 1. Admin Token
# 2. 來源商品 ID
# 3. 是否指定新標題
```

### 方法 3：使用 cURL

```bash
curl -X POST http://localhost:8080/api/admin/lottery/copy \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {YOUR_TOKEN}" \
  -d '{
    "sourceLotteryId": "123e4567-e89b-12d3-a456-426614174000"
  }'
```

---

## 📋 複製規則總結

### Lottery 主表

| 項目 | 處理方式 |
|------|----------|
| ID | ✅ 產生新 UUID |
| 標題 | ⚠️ 自動加上「（複製）」或使用自訂標題 |
| 店家 | ✅ 保持相同 |
| 分類/價格/圖片 | ✅ 完全相同 |
| 抽數統計 | ⚠️ 重置為 0 |
| 狀態 | ⚠️ 預設 OFF_SHELF |
| Remark | ⚠️ 標註「複製自商品：{原標題}」 |

### LotteryPrize（獎項）

| 項目 | 處理方式 |
|------|----------|
| ID | ✅ 產生新 UUID |
| 商品關聯 | ✅ 關聯到新商品 |
| 名稱/圖片/等級 | ✅ 完全相同 |
| 數量 | ✅ 完全相同 |
| 剩餘數量 | ⚠️ 重置為原始數量 |

---

## 💡 使用建議

### 1. 預設使用最簡化方式

```typescript
// 推薦：只傳來源 ID
await axios.post('/api/admin/lottery/copy', {
  sourceLotteryId: lotteryId
});
```

### 2. 複製後再修改

```typescript
// 先複製
const { data } = await axios.post('/api/admin/lottery/copy', {
  sourceLotteryId: lotteryId
});

// 再修改（如果需要）
await axios.put(`/api/admin/lottery/${data.data.id}`, {
  title: '新標題',
  pricePerDraw: 100
});
```

### 3. 檢查後再上架

```typescript
// 先複製（預設下架）
const { data } = await axios.post('/api/admin/lottery/copy', {
  sourceLotteryId: lotteryId
});

// 檢查無誤後上架
await axios.post(`/api/admin/lottery/${data.data.id}/on-shelf`);
```

---

## 📚 相關文件

- 📄 **完整 API 測試指南**：`LOTTERY_COPY_API_TEST_GUIDE.md`
- 📄 **實作報告**：`LOTTERY_COPY_IMPLEMENTATION_REPORT.md`
- 📦 **Postman Collection**：`KUJI_Lottery_Copy_API.postman_collection.json`
- 🔧 **測試腳本**：`test-lottery-copy.bat`

---

## ❓ 常見問題

### Q1: 可以跨店家複製嗎？

**A**: 不可以。新商品會保持原店家 ID，確保店家資料隔離。

### Q2: 複製後可以修改嗎？

**A**: 可以。使用 `PUT /api/admin/lottery/{id}` 更新商品。

### Q3: 複製後需要重新設定獎項嗎？

**A**: 不需要。所有獎項會自動複製。

### Q4: 複製後會立即上架嗎？

**A**: 不會。預設狀態為 `OFF_SHELF`，需要手動上架。

---

## 🎉 開始使用

1. **匯入 Postman Collection**
2. **執行測試流程**
3. **整合到前端專案**

---

**實作完成時間**：2026-01-09  
**狀態**：✅ 完成（無編譯錯誤）  
**測試狀態**：⏳ 待前端測試
