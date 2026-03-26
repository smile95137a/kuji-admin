# API 合約：管理員錢包調整

**功能**：`006-payment-points`  
**基礎 URL**：`/admin`  
**認證**：JWT Bearer token（管理員角色）  
**Content-Type**：`application/json`

---

## POST /admin/wallet/adjust

手動調整玩家的金幣或紅利點數餘額。基於稽核合規要求必須填寫調整原因。支援正向（贈送）和負向（扣除）調整。

### 請求

```http
POST /admin/wallet/adjust
Authorization: Bearer <admin-jwt>
Content-Type: application/json
```

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "currency": "GOLD",
  "delta": 100,
  "reason": "客服補償 — 技術問題導致抽獎失敗 (Ticket #1234)"
}
```

### 請求欄位

| 欄位 | 類型 | 必填 | 約束 |
|-------|------|----------|-------------|
| `userId` | String (UUID) | ✅ | 必須是已存在的玩家 |
| `currency` | Enum | ✅ | `GOLD` 或 `BONUS` |
| `delta` | Long | ✅ | 非零整數；正數 = 增加，負數 = 扣除 |
| `reason` | String | ✅ | 5–500 個字元；稽核用途必填 |

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "currency": "GOLD",
    "delta": 100,
    "goldBalanceAfter": 600,
    "bonusBalanceAfter": 75,
    "transactionId": "tx-uuid",
    "adminId": "admin-uuid",
    "reason": "客服補償 — 技術問題導致抽獎失敗 (Ticket #1234)",
    "adjustedAt": "2026-03-22T10:15:00Z"
  }
}
```

### 回應欄位

| 欄位 | 類型 | 說明 |
|-------|------|-------------|
| `userId` | String | 被調整的玩家 ID |
| `currency` | Enum | 被調整的幣別 |
| `delta` | Long | 實際套用的差值（與請求相同） |
| `goldBalanceAfter` | Long | 調整後玩家的金幣餘額 |
| `bonusBalanceAfter` | Long | 調整後玩家的紅利點數餘額 |
| `transactionId` | String | `WalletTransaction` 記錄 ID（type=ADMIN_ADJUST） |
| `adminId` | String | 執行此調整的管理員使用者 |
| `reason` | String | 稽核原因 |
| `adjustedAt` | ISO-8601 | 調整時間戳記 |

### 錯誤回應

| 狀態碼 | 代碼 | 情境 |
|--------|------|---------|
| 400 | 400 | 缺少欄位、`delta = 0`、`currency` 不在 enum 中 |
| 400 | 400 | `reason` 少於 5 個字元 |
| 401 | 401 | 未認證 |
| 403 | 403 | 呼叫者不具備管理員角色 |
| 404 | 404 | `userId` 不存在 |
| 422 | 422 | 負向調整將使餘額低於零 |

### 業務規則

1. **原因為必填** — 若遺失或空白則以 400 拒絕（FR-009）。
2. **不允許負餘額** — 若負向 `delta` 導致 `balance < 0`，回傳 422 並附訊息 `"Adjustment would result in negative balance"`。管理員必須減少差值。
3. **原子性 + 稽核** — 調整與 `WalletTransaction` 插入在同一 `@Transactional` 區塊中執行。交易類型固定為 `ADMIN_ADJUST`。
4. **樂觀鎖** — 與所有其他錢包操作相同，使用 `User.version` 機制。
5. **商店限制** — 此端點僅限 `/admin/*`。商店使用者（`STORE` 角色）無法存取（FR-011）。

---

## GET /admin/wallet/user/{userId}

從管理員面板檢視玩家的錢包摘要。包含餘額 + 最近交易記錄。

### 請求

```http
GET /admin/wallet/user/{userId}?txPage=0&txSize=10
Authorization: Bearer <admin-jwt>
```

### 路徑參數

| 參數 | 說明 |
|-----------|-------------|
| `userId` | 玩家的 UUID |

### 查詢參數

| 參數 | 預設值 | 說明 |
|-----------|---------|-------------|
| `txPage` | 0 | 交易歷史頁碼 |
| `txSize` | 10 | 每頁交易筆數 |

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "nickname": "玩家小明",
    "email": "xiaoming@example.com",
    "goldBalance": 500,
    "bonusBalance": 75,
    "totalRecharged": 3000,
    "transactions": {
      "content": [
        {
          "id": "tx-uuid",
          "transactionType": "ADMIN_ADJUST",
          "goldDelta": 100,
          "bonusDelta": 0,
          "goldAfter": 600,
          "bonusAfter": 75,
          "referenceId": null,
          "reason": "客服補償 — 技術問題導致抽獎失敗 (Ticket #1234)",
          "createdAt": "2026-03-22T10:15:00Z"
        }
      ],
      "totalElements": 15,
      "totalPages": 2,
      "page": 0,
      "size": 10
    }
  }
}
```

### 錯誤回應

| 狀態碼 | 情境 |
|--------|---------|
| 401 | 未認證 |
| 403 | 非管理員角色 |
| 404 | 使用者不存在 |
