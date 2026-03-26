# API 合約：儲值套餐 — 管理員端點

**功能**：`006-payment-points`  
**基礎 URL**：`/admin`  
**認證**：JWT Bearer token（管理員角色）  
**Content-Type**：`application/json`

---

## GET /admin/recharge-packages

列出所有儲值套餐（方案）。回傳啟用和停用的套餐，供管理員管理。

### 請求

```http
GET /admin/recharge-packages?isActive=true&page=0&size=20
Authorization: Bearer <admin-jwt>
```

### 查詢參數

| 參數 | 類型 | 預設值 | 說明 |
|-----------|------|---------|-------------|
| `isActive` | Boolean | （全部） | 依啟用狀態篩選（可選） |
| `page` | int | 0 | 從零開始的頁碼 |
| `size` | int | 20 | 每頁筆數 |

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "plan-uuid",
        "name": "入門包 500",
        "goldAmount": 500,
        "bonusAmount": 50,
        "priceTwd": 500.00,
        "isActive": true,
        "sortOrder": 1,
        "validFrom": null,
        "validUntil": null,
        "createdAt": "2026-03-01T00:00:00Z",
        "updatedAt": "2026-03-01T00:00:00Z"
      }
    ],
    "totalElements": 5,
    "totalPages": 1,
    "page": 0,
    "size": 20
  }
}
```

### 套餐項目欄位

| 欄位 | 類型 | 說明 |
|-------|------|-------------|
| `id` | String (UUID) | 套餐 ID |
| `name` | String | 顯示名稱（顯示給玩家） |
| `goldAmount` | Long | 購買時入帳的金幣 |
| `bonusAmount` | Long | 購買時贈送的紅利點數（0 = 無加贈） |
| `priceTwd` | Decimal | 新台幣價格 |
| `isActive` | Boolean | 是否對玩家顯示 |
| `sortOrder` | Integer | 顯示順序（升冪） |
| `validFrom` | ISO-8601 / null | 促銷開始時間（null = 無限制） |
| `validUntil` | ISO-8601 / null | 促銷結束時間（null = 永不到期） |
| `createdAt` | ISO-8601 | 記錄建立時間 |
| `updatedAt` | ISO-8601 | 最後修改時間 |

---

## POST /admin/recharge-packages

建立新的儲值套餐。

### 請求

```http
POST /admin/recharge-packages
Authorization: Bearer <admin-jwt>
Content-Type: application/json
```

```json
{
  "name": "大禮包 1000",
  "goldAmount": 1000,
  "bonusAmount": 150,
  "priceTwd": 1000.00,
  "isActive": true,
  "sortOrder": 3,
  "validFrom": null,
  "validUntil": null
}
```

### 請求欄位

| 欄位 | 類型 | 必填 | 約束 |
|-------|------|----------|-------------|
| `name` | String | ✅ | 1–100 個字元 |
| `goldAmount` | Long | ✅ | > 0 |
| `bonusAmount` | Long | ❌ | ≥ 0；預設為 0 |
| `priceTwd` | Decimal | ✅ | > 0；最多 2 位小數 |
| `isActive` | Boolean | ❌ | 預設為 `true` |
| `sortOrder` | Integer | ❌ | 預設為 0 |
| `validFrom` | ISO-8601 | ❌ | 若兩者皆提供，必須早於 `validUntil` |
| `validUntil` | ISO-8601 | ❌ | 若兩者皆提供，必須晚於 `validFrom` |

### 回應 — 201 Created

```json
{
  "code": 201,
  "message": "created",
  "data": {
    "id": "new-plan-uuid",
    "name": "大禮包 1000",
    "goldAmount": 1000,
    "bonusAmount": 150,
    "priceTwd": 1000.00,
    "isActive": true,
    "sortOrder": 3,
    "validFrom": null,
    "validUntil": null,
    "createdAt": "2026-03-22T09:00:00Z",
    "updatedAt": "2026-03-22T09:00:00Z"
  }
}
```

### 錯誤回應

| 狀態碼 | 情境 |
|--------|---------|
| 400 | 驗證失敗（缺少必填欄位、無效範圍） |
| 401 | 未認證 |
| 403 | 角色權限不足（非管理員） |

---

## PUT /admin/recharge-packages/{id}

更新現有儲值套餐。

### 請求

```http
PUT /admin/recharge-packages/{id}
Authorization: Bearer <admin-jwt>
Content-Type: application/json
```

```json
{
  "name": "大禮包 1000 (限時)",
  "goldAmount": 1000,
  "bonusAmount": 200,
  "priceTwd": 1000.00,
  "isActive": true,
  "sortOrder": 2,
  "validFrom": "2026-04-01T00:00:00Z",
  "validUntil": "2026-04-30T23:59:59Z"
}
```

所有欄位約束與 POST 相同。PUT 時所有欄位均為可選 — 僅更新已提供的欄位（部分更新 / PATCH 語意）。

### 回應 — 200 OK

回傳已更新的套餐物件（結構與 POST 回應相同）。

### 錯誤回應

| 狀態碼 | 情境 |
|--------|---------|
| 400 | 驗證失敗 |
| 401 | 未認證 |
| 403 | 角色權限不足 |
| 404 | 套餐不存在 |
| 409 | 無法修改有 PENDING 狀態 RechargeOrder 參照的套餐 |

---

## DELETE /admin/recharge-packages/{id}

軟刪除（停用）儲值套餐。將 `is_active` 設為 `false`。**不會**實際刪除資料列（儲值訂單歷史記錄仍參照該套餐）。

### 請求

```http
DELETE /admin/recharge-packages/{id}
Authorization: Bearer <admin-jwt>
```

### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "deactivated",
  "data": null
}
```

### 錯誤回應

| 狀態碼 | 情境 |
|--------|---------|
| 401 | 未認證 |
| 403 | 角色權限不足 |
| 404 | 套餐不存在 |

---

## GET /admin/recharge-packages（玩家端鏡像）

> **玩家端**列表端點為 `GET /api/recharge-plans`（現有 `RechargePlanController`）。僅回傳目前有效時間範圍內 `isActive=true` 的套餐。不需要認證（公開端點）。結構與管理員列表回應相同，但不含 `isActive`、`createdAt`、`updatedAt` 欄位。

```json
{
  "code": 200,
  "data": [
    {
      "id": "plan-uuid",
      "name": "入門包 500",
      "goldAmount": 500,
      "bonusAmount": 50,
      "priceTwd": 500.00,
      "sortOrder": 1,
      "validFrom": null,
      "validUntil": null
    }
  ]
}
```
