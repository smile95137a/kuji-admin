# 合約：管理員取消訂單

**端點**：`PUT /admin/orders/{orderId}/cancel`  
**功能**：002-express-shipping  
**執行角色**：僅管理員（ADMIN 角色）  
**使用者故事**：US4 — 後台在出貨前取消訂單

---

## 概述

允許管理員（非店家負責人）在訂單進入 `SHIPPED` 狀態前取消訂單。取消後不可逆。玩家無法直接取消 — 必須聯繫店家負責人，若有必要再由負責人向管理員申請（FR-008）。

---

## 認證與授權

| 需求 | 詳情 |
|-------------|--------|
| 認證類型 | JWT Bearer token（AdminJwtAuthenticationFilter） |
| 必要角色 | 僅 `ADMIN` — `@PreAuthorize("hasRole('ADMIN')")` |
| STORE_OWNER | ❌ 無法取消 — 將收到 403 |

---

## 請求

### 路徑參數

| 參數 | 型別 | 必填 | 說明 |
|-----------|------|----------|-------------|
| `orderId` | String (UUID) | 是 | 要取消的訂單 |

### 標頭

```
Authorization: Bearer <admin-jwt>
Content-Type: application/json
```

### 請求本體：`OrderCancelReq`

```json
{
  "reason": "顧客要求取消，商品尚未出貨"
}
```

| 欄位 | 限制條件 | 錯誤訊息 |
|-------|-----------|--------------|
| `reason` | `@NotBlank` | `取消原因不可為空` |

---

## 回應

### 200 OK — 取消成功

```json
{
  "code": 200,
  "message": "訂單已取消",
  "data": null
}
```

### 400 Bad Request — 缺少原因

```json
{
  "code": 400,
  "message": "取消原因不可為空",
  "data": null
}
```

### 403 Forbidden — 角色錯誤（STORE_OWNER 嘗試取消）

```json
{
  "code": 403,
  "message": "Access Denied",
  "data": null
}
```

### 404 Not Found

```json
{
  "code": 404,
  "message": "訂單不存在",
  "data": null
}
```

### 409 Conflict — 無法取消（已出貨或已完成）

```json
{
  "code": 409,
  "message": "訂單已出貨，無法取消",
  "data": null
}
```

---

## 副作用

| 更新欄位 | 設定值 |
|---------------|-----------|
| `order.status` | `CANCELLED` |
| `order.cancelled_at` | `NOW()` |
| `order.cancelled_by` | 已認證管理員的使用者 ID |
| `order.cancel_reason` | 請求本體中的 `reason` |
| `order.updated_at` | `NOW()` |
| `OrderStatusLog` | 新條目：`fromStatus={前一狀態}`、`toStatus=CANCELLED`、`operatorType=ADMIN` |

---

## 可取消狀態矩陣

| 當前狀態 | 可取消？ | 原因 |
|---------------|-------------|--------|
| `PENDING` | ✅ 可以 | `isCancellable() = true` |
| `PREPARING` | ✅ 可以 | `isCancellable() = true` |
| `SHIPPED` | ❌ 不可 | `isCancellable() = false` — 訂單正在配送中 |
| `COMPLETED` | ❌ 不可 | `isFinished() = true` — 配送已完成 |
| `CANCELLED` | ❌ 不可 | 已取消 |

---

## 業務規則

1. 只有 `ADMIN` 角色的使用者可呼叫此端點（FR-008）。`STORE_OWNER` 明確排除在外。
2. 僅在 `order.status` 為 `PENDING` 或 `PREPARING` 時允許取消（FR-007）。
3. 一旦取消，無法進行任何進一步的狀態轉換。
4. `cancelled_by` 欄位記錄執行取消的管理員，以確保可追責性。
5. 取消原因為必填，以提供稽核追蹤並向店家負責人說明原因。

---

## 實作備註

- **控制器**：`AdminOrderController.java` → `cancel()` 方法已存在，帶有 `@PreAuthorize("hasRole('ADMIN')")`。
- **服務**：`OrderService.cancel(String orderId, OrderCancelReq req, String operatorId)` — 已實作。
- **守衛**：在 `OrderServiceImpl.cancel()` 中檢查 `OrderStatusEnum.isCancellable()`。
- **測試**：`AdminOrderControllerTest.java` — 目前為空；必須補充以下測試：
  - ADMIN 可取消 PENDING 訂單 → 200
  - ADMIN 可取消 PREPARING 訂單 → 200
  - ADMIN 不能取消 SHIPPED 訂單 → 409
  - STORE_OWNER 不能取消 → 403
  - 無取消原因 → 400
