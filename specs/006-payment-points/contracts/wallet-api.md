# API 合約：錢包 — 玩家端點

**功能**：`006-payment-points`  
**基礎 URL**：`/api`  
**認證**：JWT Bearer token（玩家角色）  
**Content-Type**：`application/json`

---

## GET /api/wallet

取得已認證玩家目前的錢包餘額。

### 請求

```http
GET /api/wallet
Authorization: Bearer <jwt>
```

無請求本體。

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "goldBalance": 1500,
    "bonusBalance": 75,
    "totalRecharged": 3000
  }
}
```

### 回應欄位

| 欄位 | 類型 | 說明 |
|-------|------|-------------|
| `userId` | String (UUID) | 已認證的玩家 ID |
| `goldBalance` | Long | 目前金幣餘額（可購買幣別） |
| `bonusBalance` | Long | 目前紅利點數餘額（贏取幣別） |
| `totalRecharged` | Long | 累計購買金幣總量（分析用） |

### 錯誤回應

| 狀態碼 | 代碼 | 情境 |
|--------|------|---------|
| 401 | 401 | JWT 遺失或無效 |
| 404 | 404 | 錢包/使用者不存在（有效 JWT 下不應發生） |

---

## POST /api/wallet/recharge

發起信用卡儲值。建立一筆 `RechargeOrder`（status=PENDING）並回傳業者付款 URL。

### 請求

```http
POST /api/wallet/recharge
Authorization: Bearer <jwt>
Content-Type: application/json
```

```json
{
  "planId": "plan-uuid-here"
}
```

### 請求欄位

| 欄位 | 類型 | 必填 | 約束 |
|-------|------|----------|-------------|
| `planId` | String (UUID) | ✅ | 必須是有效且目前在時間範圍內的 `RechargePlan` |

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "rechargeOrderId": "ro-uuid-here",
    "payUrl": "https://payment.gateway.com/pay/abc123",
    "goldAmount": 500,
    "bonusAmount": 50,
    "priceTwd": 500.00,
    "expiredAt": "2026-03-22T10:30:00Z"
  }
}
```

### 回應欄位

| 欄位 | 類型 | 說明 |
|-------|------|-------------|
| `rechargeOrderId` | String (UUID) | 內部訂單 ID（用於查詢/除錯） |
| `payUrl` | String | 重新導向至業者付款頁面的 URL |
| `goldAmount` | Long | 成功後入帳的金幣 |
| `bonusAmount` | Long | 成功後入帳的紅利點數 |
| `priceTwd` | Decimal | 收取的新台幣金額 |
| `expiredAt` | ISO-8601 | 此訂單的到期時間（30 分鐘 TTL） |

### 錯誤回應

| 狀態碼 | 代碼 | 情境 |
|--------|------|---------|
| 400 | 400 | `planId` 遺失或 UUID 格式錯誤 |
| 401 | 401 | 未認證 |
| 404 | 404 | 套餐不存在或未啟用 |
| 422 | 422 | 套餐不在有效時間範圍內 |
| 502 | 502 | 業者建立付款工作階段失敗 |

---

## POST /api/wallet/recharge/callback

金流業者 webhook 回呼。付款完成後由業者**伺服器對伺服器**呼叫。

> ⚠️ 此端點**不得**要求 JWT 認證。僅由業者簽章驗證來保護。此端點必須在 Spring Security 過濾鏈中加入白名單。

### 請求

業者專屬載荷（TapPay 風格範例）：

```http
POST /api/wallet/recharge/callback
Content-Type: application/json
X-Gateway-Signature: sha256=<hmac-signature>
```

```json
{
  "merchantOrderId": "ro-uuid-here",
  "status": 0,
  "statusMessage": "Success",
  "transactionId": "gateway-tx-id-12345",
  "amount": 50000,
  "currency": "TWD",
  "paidTime": "2026-03-22T10:05:23Z"
}
```

> 注意：各業者的欄位名稱不同。`PaymentGatewayClient` 介面卡會在服務層處理前，將所有業者載荷正規化為內部 `GatewayCallbackResult` DTO。

### 內部 DTO：GatewayCallbackResult

```java
public record GatewayCallbackResult(
    String merchantOrderId,  // = RechargeOrder.id
    boolean success,         // gateway status == success
    String gatewayOrderId,   // gateway's own transaction ID
    BigDecimal amountTwd,    // amount charged
    LocalDateTime paidAt,    // payment timestamp
    String rawPayload        // full JSON for audit storage
) {}
```

### 回應 — 200 OK

務必回傳 `200` 給業者（即使是重複/已處理的回呼），以防止重試風暴。

```json
{ "result": "OK" }
```

### 處理邏輯

1. 使用共享密鑰驗證 HMAC 簽章（業者專屬）。
2. 透過 `merchantOrderId` 查詢 `RechargeOrder`。
3. 若 `status != PENDING` → 立即回傳 `200 OK`（冪等略過）。
4. 若業者回報失敗 → 將狀態更新為 `FAILED`，回傳 `200 OK`。
5. 若業者回報成功：
   - `BEGIN TRANSACTION`
   - `UPDATE recharge_order SET status='SUCCESS', paid_at=? WHERE id=? AND status='PENDING'`
   - 若 `rowsAffected == 0` → 並發回呼已處理 → `COMMIT` → `return 200`
   - `UPDATE users SET gold_coins=?, bonus_coins=?, version=version+1 WHERE id=? AND version=?`
   - `INSERT INTO wallet_transaction (...)`
   - `COMMIT`
6. 回傳 `200 OK`。

### 錯誤回應

| 狀態碼 | 情境 |
|--------|---------|
| 400 | 簽章無效 — 記錄日誌並回傳 400（不處理） |
| 500 | 意外的伺服器錯誤（業者將重試） |

---

## GET /api/wallet/transactions

取得已認證玩家的分頁交易歷史。

### 請求

```http
GET /api/wallet/transactions?page=0&size=20&type=RECHARGE
Authorization: Bearer <jwt>
```

### 查詢參數

| 參數 | 類型 | 預設值 | 說明 |
|-----------|------|---------|-------------|
| `page` | int | 0 | 從零開始的頁碼 |
| `size` | int | 20 | 每頁筆數（最大 100） |
| `type` | String | （全部） | 依交易類型篩選（可選） |
| `startDate` | ISO-8601 | （無） | 篩選起始日期（可選） |
| `endDate` | ISO-8601 | （無） | 篩選結束日期（可選） |

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "tx-uuid",
        "transactionType": "RECHARGE",
        "goldDelta": 500,
        "bonusDelta": 50,
        "goldAfter": 1500,
        "bonusAfter": 75,
        "referenceId": "ro-uuid",
        "reason": null,
        "createdAt": "2026-03-22T10:05:23.456Z"
      }
    ],
    "totalElements": 42,
    "totalPages": 3,
    "page": 0,
    "size": 20
  }
}
```

### 交易項目欄位

| 欄位 | 類型 | 說明 |
|-------|------|-------------|
| `id` | String (UUID) | 交易 ID |
| `transactionType` | Enum | RECHARGE / DRAW / BONUS_GRANT / RECYCLE / ADMIN_ADJUST / REFUND |
| `goldDelta` | Long | 金幣變動量（負數為扣款） |
| `bonusDelta` | Long | 紅利點數變動量（負數為扣款） |
| `goldAfter` | Long | 此交易後的金幣餘額 |
| `bonusAfter` | Long | 此交易後的紅利點數餘額 |
| `referenceId` | String | 相關實體 ID（訂單、recharge_order、prize_box） |
| `reason` | String | 說明（ADMIN_ADJUST 時填入） |
| `createdAt` | ISO-8601 | 交易時間戳記（毫秒精度） |

### 錯誤回應

| 狀態碼 | 情境 |
|--------|---------|
| 400 | `type` enum 值無效 |
| 401 | 未認證 |

---

## POST /api/prize-box/recycle

將玩家獎品盒中未出貨的獎品回收，換取紅利點數。此操作為永久性且不可逆。

### 請求

```http
POST /api/prize-box/recycle
Authorization: Bearer <jwt>
Content-Type: application/json
```

```json
{
  "prizeBoxId": "pb-uuid-here"
}
```

### 請求欄位

| 欄位 | 類型 | 必填 | 約束 |
|-------|------|----------|-------------|
| `prizeBoxId` | String (UUID) | ✅ | 必須屬於已認證使用者，status=AVAILABLE，is_recyclable=1 |

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "prizeBoxId": "pb-uuid-here",
    "bonusAwarded": 20,
    "newBonusBalance": 95,
    "transactionId": "tx-uuid"
  }
}
```

### 回應欄位

| 欄位 | 類型 | 說明 |
|-------|------|-------------|
| `prizeBoxId` | String | 已回收的獎品盒項目 |
| `bonusAwarded` | Long | 已入帳的紅利點數 |
| `newBonusBalance` | Long | 回收後玩家的紅利點數餘額 |
| `transactionId` | String | 稽核交易 ID（RECYCLE 類型） |

### 錯誤回應

| 狀態碼 | 代碼 | 情境 |
|--------|------|---------|
| 400 | 400 | `prizeBoxId` 遺失或格式錯誤 |
| 401 | 401 | 未認證 |
| 403 | 403 | 獎品屬於其他使用者 |
| 404 | 404 | 獎品盒項目不存在 |
| 409 | 409 | 獎品非 AVAILABLE 狀態（已出貨、已回收等） |
| 422 | 422 | `is_recyclable = 0` — 此獎品類型無法回收 |
