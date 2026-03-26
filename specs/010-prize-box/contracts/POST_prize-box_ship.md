# API 契約： POST /api/prize-box/ship

**功能**：獎品盒 (010-prize-box)  
**端點**：`POST /api/prize-box/ship`  
**用途**：選取獎品盒中的獎品，填寫收件地址，建立出貨訂單（自動按店家拆單）

---

## 概覽

玩家選取一或多件獎品並確認收件地址後，系統原子性地將獎品從獎品盒移除並建立出貨訂單。不同店家的獎品自動拆分為獨立訂單。

---

## 驗證

**必要性**：✅ JWT Bearer Token  
**標頭**：`Authorization: Bearer <token>`

---

## 請求

```http
POST /api/prize-box/ship
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

### 請求 Body

```json
{
  "prizeBoxIds": ["prizebox-uuid-001", "prizebox-uuid-002", "prizebox-uuid-003"],
  "shippingMethod": "HOME_DELIVERY",
  "userAddressId": "address-uuid-001",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區信義路五段7號",
  "storeCode": null,
  "storeName": null,
  "storeAddress": null,
  "remark": "請勿對折"
}
```

### 請求 Fields

| 欄位 | 型別 | 必填 | 說明 |
|-------|------|----------|-------------|
| `prizeBoxIds` | Array\<String\> | ✅ | 要出貨的獎品盒 ID 列表（至少 1 筆）|
| `shippingMethod` | String (enum) | ✅ | `HOME_DELIVERY` / `SEVEN_ELEVEN` / `FAMILY_MART` |
| `userAddressId` | String (UUID) | ❌ | 已儲存地址 ID（P3：有此值時優先使用）|
| `recipientName` | String | △ | 收件人姓名（若無 userAddressId 且 User 無預設則必填）|
| `recipientPhone` | String | △ | 收件人電話（同上）|
| `recipientAddress` | String | △ | 收件地址（HOME_DELIVERY 且無 userAddressId 則必填）|
| `storeCode` | String | △ | 超商店號（超商取貨必填）|
| `storeName` | String | △ | 超商店名（超商取貨必填）|
| `storeAddress` | String | △ | 超商地址（超商取貨必填）|
| `remark` | String | ❌ | 備註（可空）|

### 地址優先順序邏輯

```
if userAddressId provided:
  → 查詢 UserAddress，填入 recipientName/Phone/Address
elif recipientName/Phone in request:
  → 使用請求提供的值
else:
  → 從 User.recipientName/recipientPhone 帶入
  → 若仍為空 → 拋出 BusinessException
```

---

## 回應

### 成功 — 200 OK

回傳建立的訂單 ID 列表（每家店家一筆）

```json
["order-uuid-001", "order-uuid-002"]
```

> 範例：`prizeBoxIds` 含來自 2 家店家的獎品 → 回傳 2 個 orderId

### 驗證錯誤 — 400 Bad Request

```json
{
  "code": 400,
  "message": "請選擇要出貨的獎品"
}
```

### 業務邏輯錯誤 — 400 Bad Request

```json
{
  "code": 400,
  "message": "獎品已處理：prizebox-uuid-001"
}
```

```json
{
  "code": 400,
  "message": "此獎品不可出貨：prizebox-uuid-002"
}
```

```json
{
  "code": 400,
  "message": "收件人姓名不可為空，請先在個人資料填寫或於出貨時提供"
}
```

### 未授權 — 401

```json
{
  "code": 401,
  "message": "未授權，請重新登入"
}
```

---

## 業務規則

1. **Owner Check**: 每件 `prizeBoxId` 的 `userId` 必須等於當前登入玩家
2. **Status Check**: 每件獎品狀態必須為 `IN_BOX`（否則 400）
3. **Shippable Check**: 每件獎品 `isShippable = 1`（否則 400 `此獎品不可出貨`）
4. **Auto-split**: 不同 `storeId` 的獎品自動建立獨立 `Order`（FR-004）
5. **Atomicity**: 整個操作在單一 `@Transactional` 中執行（FR-005）
6. **Status Update**: 成功後 `PrizeBox.status → SHIPPED`，`shippedAt` 設為當前時間
7. **Disabled Store**: 若店家 `status = DISABLED`，拒絕建立新訂單（邊界情況）

---

## 自動拆單範例

```
Request prizeBoxIds: [A1(店家X), A2(店家X), B1(店家Y)]

→ 分組: { 店家X: [A1, A2], 店家Y: [B1] }
→ 建立:
    Order-001 (storeId=店家X): items=[A1, A2]
    Order-002 (storeId=店家Y): items=[B1]
→ 回傳: ["Order-001", "Order-002"]
```

---

## 實作流程

```
1. SecurityUtils.getCurrentUserId()
2. 驗證 prizeBoxIds 非空
3. for each prizeBoxId:
   a. SELECT PrizeBox WHERE id = ?
   b. 驗證 userId == currentUser
   c. 驗證 status == IN_BOX
   d. 驗證 isShippable == 1
4. 若提供 userAddressId → 查詢 UserAddress 填入收件欄位
   否則 → 從 User 個人資料帶入（現有邏輯）
5. 按 storeId 分組
6. for each storeGroup:
   OrderService.createOrdersFromPrizeBox(...)
7. UPDATE prize_box SET status='SHIPPED', shipped_at=NOW() WHERE id IN (...)
8. 回傳 orderIds
```

---

## 測試情境

| # | 情境 | 預期結果 |
|---|----------|----------|
| 1 | 同一店家 2 件獎品 | 建立 1 筆訂單，回傳 1 個 orderId |
| 2 | 兩家店家各 1 件 | 建立 2 筆訂單，回傳 2 個 orderId |
| 3 | 包含已出貨獎品 | 400 `獎品已處理` |
| 4 | 包含 isShippable=0 的獎品 | 400 `此獎品不可出貨` |
| 5 | 使用 userAddressId | 訂單使用該地址資訊 |
| 6 | 無地址且 User 無預設 | 400 `收件人姓名不可為空` |
| 7 | 操作他人獎品 | 400 `無權操作此獎品` |
| 8 | prizeBoxIds 為空 | 400 validation error |
| 9 | 已停用店家的獎品 | 400 `店家已停用，無法建立訂單` |
