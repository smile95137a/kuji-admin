# 合約：PUT /admin/stores/{id}/status

**用途**：啟用或停用店家。停用時觸發連鎖：所有商品 → `OFF_SHELF`，所有橫幅 → `DISABLED`。  
**認證**：僅限 `ROLE_ADMIN`  
**路由**：`PUT /admin/stores/{id}/status`

---

## 請求

### 路徑參數
| 參數 | 型別 | 說明 |
|-------|------|-------------|
| `id` | UUID string | 店家 ID |

### 查詢參數（選填）
| 參數 | 型別 | 預設值 | 說明 |
|-------|------|---------|-------------|
| `force` | boolean | `false` | 略過進行中抽獎活動警告並直接執行 |

### Headers
```
Authorization: Bearer <admin-jwt>
Content-Type: application/json
```

### Body
```json
{
  "status": "DISABLED"
}
```
或：
```json
{
  "status": "ENABLED"
}
```

**允許的值**：`ENABLED`、`DISABLED`

---

## 回應 — 停用流程

### 步驟 1：第一次呼叫（不帶 `?force=true`）
若店家有進行中（ON_SHELF）的抽獎活動：

**HTTP 409 Conflict**
```json
{
  "code": "ACTIVE_LOTTERIES",
  "message": "店家有 5 個進行中的抽獎活動，確認停用請加上 ?force=true",
  "activeLotteryCount": 5
}
```

### 步驟 2：確認呼叫（帶 `?force=true`）

**HTTP 200 OK**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "DISABLED",
  "cascadeResult": {
    "productsOffShelf": 10,
    "bannersDisabled": 2
  },
  "updatedAt": "2026-03-22T12:00:00"
}
```

### 無進行中抽獎活動（立即執行停用）

**HTTP 200 OK**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "DISABLED",
  "cascadeResult": {
    "productsOffShelf": 3,
    "bannersDisabled": 0
  },
  "updatedAt": "2026-03-22T12:00:00"
}
```

---

## 回應 — 啟用流程

**HTTP 200 OK**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "ENABLED",
  "cascadeResult": null,
  "updatedAt": "2026-03-22T13:00:00",
  "note": "商品與橫幅狀態未自動恢復，需手動重新啟用"
}
```

> **重要**：重新啟用**不會**恢復商品或橫幅。啟用時 `cascadeResult` 為 `null`。

---

## 錯誤回應

### 400 Bad Request — 無效的狀態值
```json
{
  "code": "VALIDATION_ERROR",
  "message": "status 只允許 ENABLED 或 DISABLED"
}
```

### 403 Forbidden — 非管理員呼叫者
```json
{
  "code": "ACCESS_DENIED",
  "message": "只有管理員可以更改店家狀態"
}
```

### 404 Not Found
```json
{
  "code": "STORE_NOT_FOUND",
  "message": "店家不存在"
}
```

---

## 連鎖交易詳情

```
PUT /admin/stores/{id}/status  { "status": "DISABLED" }

BEGIN TRANSACTION
  1. UPDATE store SET status='DISABLED', updated_at=NOW(), updated_by=? WHERE id=?
  2. UPDATE lottery SET status='OFF_SHELF', updated_at=NOW()
        WHERE store_id=? AND status NOT IN ('OFF_SHELF','DRAFT')
     → returns affected rows count (productsOffShelf)
  3. UPDATE news_banner SET status='DISABLED', updated_at=NOW()
        WHERE store_id=? AND status='ENABLED'
     → returns affected rows count (bannersDisabled)
COMMIT

PUT /admin/stores/{id}/status  { "status": "ENABLED" }

BEGIN TRANSACTION
  1. UPDATE store SET status='ENABLED', updated_at=NOW(), updated_by=? WHERE id=?
     (No cascade — products and banners unchanged)
COMMIT
```

---

## 安全性

- `@PreAuthorize("hasRole('ADMIN')")` — 只有平台管理員可以變更店家狀態。
- `updatedBy` 從 JWT 的 `SecurityUtils.getCurrentUserId()` 自動帶入。
