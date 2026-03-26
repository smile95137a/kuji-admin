# 合約：POST /prize-box/ship — 出貨獎品並建立訂單

**功能**：`004-game-to-order`  
**Controller**：`com.group.admin.controller.api.PrizeBoxController`  
**認證**：必須 — `Authorization: Bearer <jwt>`

---

## 端點

```
POST /prize-box/ship
```

> 客戶端的完整路徑：`POST /api/prize-box/ship`

---

## 用途

允許已認證的玩家選取一或多件 `IN_BOX` 狀態的獎品盒項目並建立出貨訂單。若所選項目分屬**多個店家**，系統會自動**每個店家各建立一筆訂單**。所有選取的獎品盒項目將從 `IN_BOX` 轉換為 `SHIPPED`。

---

## 認證與授權

| 需求 | 細節 |
|-------------|--------|
| 需要認證 | 是 — `Authorization: Bearer` header 中的 JWT |
| 適用範圍 | 僅限玩家（`userType = "user"`） |
| 擁有者強制驗證 | 每個 `prizeBoxId` 都會驗證屬於已認證使用者；任何非本人的項目將觸發 400 錯誤 |

---

## 請求

### Headers

| Header | 必填 | 範例 |
|--------|----------|---------|
| `Authorization` | 是 | `Bearer eyJhbGci...` |
| `Content-Type` | 是 | `application/json` |

### 請求體 — PrizeBoxShipReq

```json
{
  "prizeBoxIds": [
    "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "b2c3d4e5-f6a7-8901-bcde-f12345678901"
  ],
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區市府路1號",
  "storeCode": null,
  "storeName": null,
  "storeAddress": null,
  "remark": "請輕放，謝謝"
}
```

### 請求欄位說明

| 欄位 | 類型 | 必填 | 驗證 | 說明 |
|-------|------|----------|-----------|-------------|
| `prizeBoxIds` | `string[]` | **是** | `@NotEmpty` — 至少需要 1 筆 | 要出貨的獎品盒 ID |
| `shippingMethod` | `string` | **是** | `@NotBlank`；必須為有效的 `ShippingMethodEnum` | `HOME_DELIVERY` / `SEVEN_ELEVEN` / `FAMILY_MART` |
| `recipientName` | `string` | 否 | 若省略則自動從使用者資料填入；仍為空白時報錯 | 收件人全名 |
| `recipientPhone` | `string` | 否 | 若省略則自動從使用者資料填入；仍為空白時報錯 | 收件人手機號碼 |
| `recipientAddress` | `string` | 條件必填 | `shippingMethod = HOME_DELIVERY` 時必填 | 完整配送地址 |
| `storeCode` | `string` | 條件必填 | `shippingMethod = SEVEN_ELEVEN / FAMILY_MART` 時必填 | 超商門市代碼 |
| `storeName` | `string` | 條件必填 | `shippingMethod = SEVEN_ELEVEN / FAMILY_MART` 時必填 | 超商門市名稱 |
| `storeAddress` | `string` | 條件必填 | `shippingMethod = SEVEN_ELEVEN / FAMILY_MART` 時必填 | 超商門市地址 |
| `remark` | `string` | 否 | 最多 500 字 | 可選備註 |

---

## 伺服器端處理流程

```
1. Extract userId from JWT
2. Auto-fill recipientName / recipientPhone from User profile (if blank)
3. For each prizeBoxId:
   a. Load PrizeBox
   b. Assert PrizeBox.userId == currentUserId           → 400 if mismatch
   c. Assert PrizeBox.status == IN_BOX                  → 400 if SHIPPED/RECYCLED
4. Group prizeBoxIds by PrizeBox.storeId
5. For each store group:
   a. Call OrderService.createOrdersFromPrizeBox(userId, storeGroupIds, …)
   b. Creates Order + OrderItem rows inside @Transactional
6. Update all PrizeBox rows: status = SHIPPED, shippedAt = now(), orderId = orderId
7. Return list of created order IDs
All steps run inside a single @Transactional boundary.
```

---

## 回應

### 200 OK — 成功

回傳已建立的訂單 ID 陣列（每個店家各一筆）。

```json
{
  "code": 200,
  "message": "出貨成功",
  "data": [
    "ord-uuid-store-a",
    "ord-uuid-store-b"
  ]
}
```

### 400 Bad Request — 驗證錯誤

```json
{
  "code": 400,
  "message": "請選擇要出貨的獎品",
  "data": null
}
```

其他可能的 400 訊息：
- `"請選擇配送方式"`
- `"獎品不屬於您"`
- `"獎品已出貨，無法再次出貨"`
- `"宅配方式必須填寫收件地址"`
- `"超商取貨必須選擇門市"`
- `"請填寫收件人姓名及電話"`

### 401 Unauthorized

```json
{
  "code": 401,
  "message": "未登入或 Token 已過期",
  "data": null
}
```

### 500 Internal Server Error（交易回滾）

```json
{
  "code": 500,
  "message": "出貨失敗，請稍後再試",
  "data": null
}
```

---

## 多店家訂單分單行為

| 情境 | 結果 |
|----------|--------|
| 所有項目來自店家 A | 為店家 A 建立 1 筆訂單 |
| 項目來自店家 A + 店家 B | 建立 2 筆訂單（每個店家各一筆） |
| 項目來自店家 A、B、C | 建立 3 筆訂單 |

玩家將在回應陣列中收到所有已建立的訂單 ID。

---

## 冪等性與安全性

- `prizeBoxIds` 中若包含 `status = SHIPPED` 或 `RECYCLED` 的項目，將**拒絕整個請求**（400 錯誤 — 交易未開始）。
- `userId ≠ currentUserId` 的項目將**拒絕整個請求**（400 錯誤）。
- 若任何單筆訂單建立失敗（資料庫錯誤），整個交易回滾 — 不會有獎品被標記為 SHIPPED，也不會有訂單被持久化。

---

## 相關端點：POST /prize-box/recycle

兌換而非出貨的相似流程。本合約不加以說明。

---

## 實作參考

| 層級 | 類別 | 方法 |
|-------|-------|--------|
| Controller | `PrizeBoxController` | `shipPrizes(@RequestBody PrizeBoxShipReq req)` |
| Service | `PrizeBoxServiceImpl` | `shipPrizes(String userId, PrizeBoxShipReq req)` |
| Service | `OrderServiceImpl` | `createOrdersFromPrizeBox(String userId, List<String> prizeBoxIds, …)` |
| Mapper | `PrizeBoxMapper` | `updateByPrimaryKey()` |
| Mapper | `OrderMapper` | `insertSelective()` |
| Mapper | `OrderItemMapper` | `insertSelective()` |
| DTO（請求） | `PrizeBoxShipReq` | 請求體 |
