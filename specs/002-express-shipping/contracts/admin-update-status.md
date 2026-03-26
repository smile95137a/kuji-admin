# 合約：管理員更新訂單狀態

**端點**（細粒度狀態轉換）：
- `PUT /admin/orders/{orderId}/prepare` — PENDING → PREPARING
- `PUT /admin/orders/{orderId}/ship` — PREPARING → SHIPPED
- `PUT /admin/orders/{orderId}/complete` — SHIPPED → COMPLETED

**功能**：002-express-shipping  
**執行角色**：店家負責人 / 管理員（STORE_OWNER 或 ADMIN 角色）  
**使用者故事**：US2 — 店家管理出貨狀態

---

## 概述

店家負責人與管理員透過出貨流程推進訂單。每個端點對應一個有效的狀態轉換。狀態機嚴格單向；逆向轉換在服務層拒絕。管理員取消操作在獨立的合約中說明（`admin-cancel-order.md`）。

---

## 認證與授權

| 需求 | 詳情 |
|-------------|--------|
| 認證類型 | JWT Bearer token（AdminJwtAuthenticationFilter） |
| 必要角色 | `ADMIN` 或 `STORE_OWNER`（類別層級 `@PreAuthorize`） |
| 店家範圍 | STORE_OWNER 只能管理屬於其店家的訂單 |

---

## 端點一：備貨

### `PUT /admin/orders/{orderId}/prepare`

將訂單標記為備貨中（已確認庫存）。

**轉換**：`PENDING` → `PREPARING`

#### 請求

```
PUT /admin/orders/550e8400-e29b-41d4-a716-446655440000/prepare
Authorization: Bearer <admin-jwt>
```

無需請求本體。

#### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "訂單已更新為備貨中",
  "data": null
}
```

#### 回應 — 409 Conflict（無效轉換）

```json
{
  "code": 409,
  "message": "訂單狀態不允許此操作",
  "data": null
}
```

---

## 端點二：出貨

### `PUT /admin/orders/{orderId}/ship`

記錄物流追蹤單號並將訂單標記為已出貨。

**轉換**：`PREPARING` → `SHIPPED`

#### 請求本體：`OrderShipReq`

```json
{
  "trackingNo": "1234567890",
  "remark": "黑貓宅急便"
}
```

| 欄位 | 限制條件 | 錯誤訊息 |
|-------|-----------|--------------|
| `trackingNo` | `@NotBlank` | `物流單號不可為空` |
| `remark` | 可選 | — |

#### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "訂單已出貨",
  "data": null
}
```

#### 副作用
- 設定 `order.tracking_no` 與 `order.shipped_at = NOW()`。
- 附加 `OrderStatusLog` 條目：`fromStatus=PREPARING`、`toStatus=SHIPPED`。

---

## 端點三：完成訂單

### `PUT /admin/orders/{orderId}/complete`

將訂單標記為已送達 / 已完成。

**轉換**：`SHIPPED` → `COMPLETED`

#### 請求

無需請求本體。

#### 回應 — 200 OK

```json
{
  "code": 200,
  "message": "訂單已完成",
  "data": null
}
```

#### 副作用
- 設定 `order.completed_at = NOW()`。
- 附加 `OrderStatusLog` 條目：`fromStatus=SHIPPED`、`toStatus=COMPLETED`。

---

## 共用錯誤回應（三個端點均適用）

### 403 Forbidden

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

### 409 Conflict — 狀態不符合轉換條件

```json
{
  "code": 409,
  "message": "訂單狀態不允許此操作",
  "data": null
}
```

---

## 狀態機摘要

```
PENDING ──prepare()──► PREPARING ──ship()──► SHIPPED ──complete()──► COMPLETED
                                                │
                                         (no reverse allowed)
```

| 端點 | 必要當前狀態 | 設定目標狀態 | 設定時間戳記 |
|----------|------------------------|----------------|---------------|
| `/prepare` | `PENDING` | `PREPARING` | `updated_at` |
| `/ship` | `PREPARING` | `SHIPPED` | `shipped_at`、`updated_at` |
| `/complete` | `SHIPPED` | `COMPLETED` | `completed_at`、`updated_at` |

---

## 業務規則

1. 每次轉換僅在緊接著的前一個狀態時有效（不允許跳過）。
2. `CANCELLED` 或 `COMPLETED` 的訂單無法進一步轉換。
3. 每次成功轉換都會附加一筆不可變的 `OrderStatusLog` 記錄（FR-010）。
4. `STORE_OWNER` 不能取消訂單 — 取消操作保留給 `ADMIN`（FR-008）。

---

## 實作備註

- **控制器**：`AdminOrderController.java` — 三個方法均已實作。
- **服務**：`OrderService.prepareShipping()`、`ship()`、`complete()` — 均已實作。
- **稽核**：在每個服務方法中呼叫 `OrderStatusLogMapper.insert()`。
