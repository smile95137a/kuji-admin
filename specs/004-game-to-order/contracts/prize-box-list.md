# 合約：GET /prize-box — 列出玩家的獎品盒項目

**功能**：`004-game-to-order`  
**Controller**：`com.group.admin.controller.api.PrizeBoxController`  
**認證**：必須 — `Authorization: Bearer <jwt>`

---

## 端點

```
GET /prize-box
```

> **備註**：應用程式的 context path 為 `/api`，因此客戶端的完整路徑為 `GET /api/prize-box`。Controller 映射至 context root 下的 `/prize-box`。

---

## 用途

回傳已認證玩家目前存放於獎品盒（`IN_BOX` 狀態）且已準備出貨的獎品項目。已出貨或已兌換的項目**不包含**在此清單中 — 它們改由訂單歷史頁面顯示。

---

## 認證與授權

| 需求 | 細節 |
|-------------|--------|
| 需要認證 | 是 — `Authorization: Bearer` header 中的 JWT |
| 適用範圍 | 僅限玩家（`userType = "user"`） |
| 擁有者強制驗證 | Service 依 JWT 透過 `SecurityUtils.getCurrentUserId()` 取得 `userId` 過濾結果 |

---

## 請求

### Headers

| Header | 必填 | 範例 |
|--------|----------|---------|
| `Authorization` | 是 | `Bearer eyJhbGci...` |
| `Content-Type` | 否 | GET 請求不需要 |

### 查詢參數

無。（v1.0 中狀態篩選硬編碼為 `IN_BOX`。）

### 請求體

無。

---

## 回應

### 200 OK — 成功

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "userId": "u1234567-89ab-cdef-0123-456789abcdef",
      "lotteryId": "l1234567-89ab-cdef-0123-456789abcdef",
      "lotteryTitle": "春節限定一番賞",
      "lotteryImageUrl": "https://s3.amazonaws.com/kuji/lottery/spring-banner.jpg",
      "prizeId": "p1234567-89ab-cdef-0123-456789abcdef",
      "prizeName": "A賞 — 特製抱枕",
      "prizeLevel": "A",
      "prizeImageUrl": "https://s3.amazonaws.com/kuji/prizes/pillow-a.jpg",
      "storeId": "s1234567-89ab-cdef-0123-456789abcdef",
      "storeName": "台北旗艦店",
      "status": "IN_BOX",
      "statusName": "在獎品盒中",
      "isRecyclable": true,
      "recycleBonus": 500,
      "createdAt": "2026-03-22T14:30:00"
    }
  ]
}
```

### 回應欄位說明

| 欄位 | 類型 | 說明 |
|-------|------|-------------|
| `id` | `string (UUID)` | PrizeBox 記錄 ID |
| `userId` | `string (UUID)` | 玩家（擁有者）ID |
| `lotteryId` | `string (UUID)` | 來源抽獎 ID |
| `lotteryTitle` | `string` | 抽獎的顯示名稱 |
| `lotteryImageUrl` | `string (URL)` | 抽獎橫幅圖片 |
| `prizeId` | `string (UUID)` | 獎品目錄 ID |
| `prizeName` | `string` | 人類可讀的獎品名稱 |
| `prizeLevel` | `string` | 獎品等級標籤（例如 `"A"`、`"B"`、`"Last"`） |
| `prizeImageUrl` | `string (URL)` | 獎品產品圖片 |
| `storeId` | `string (UUID)` | 所屬店家 — 用於每店家訂單分單 |
| `storeName` | `string` | 店家顯示名稱 |
| `status` | `string` | v1.0 中永遠為 `"IN_BOX"`（硬編碼篩選） |
| `statusName` | `string` | v1.0 中永遠為 `"在獎品盒中"` |
| `isRecyclable` | `boolean` | 此項目是否可兌換 |
| `recycleBonus` | `number` | 兌換後獲得的積分（可能為 null） |
| `createdAt` | `ISO-8601 datetime` | 項目加入獎品盒的時間 |

### 200 OK — 空清單

玩家沒有 IN_BOX 項目時：

```json
{
  "code": 200,
  "message": "success",
  "data": []
}
```

### 401 Unauthorized

```json
{
  "code": 401,
  "message": "未登入或 Token 已過期",
  "data": null
}
```

### 500 Internal Server Error

```json
{
  "code": 500,
  "message": "系統錯誤，請稍後再試",
  "data": null
}
```

---

## 排序

結果依 `created_at DESC` 排序（最近加入的獎品排在最前面）。

---

## 已知限制（v1.0）

- 無分頁 — 一次回傳所有 IN_BOX 項目。在預期規模（每位玩家少於 100 筆）下可接受。
- 無狀態篩選參數 — 永遠只回傳 `IN_BOX`。
- 無搜尋或排序覆寫參數。

---

## 實作參考

| 層級 | 類別 | 方法 |
|-------|-------|--------|
| Controller | `PrizeBoxController` | `getMyPrizeBox()` |
| Service | `PrizeBoxServiceImpl` | `getPrizeBox(String userId)` |
| Mapper | `PrizeBoxMapper` | `selectByExample(PrizeBoxExample)` |
| DTO | `PrizeBoxItemRes` | 回應項目 |
