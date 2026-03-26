# 資料模型：物流與出貨管理 (Express Shipping)

**功能分支**：`002-express-shipping`  
**產生日期**：2026-03-22  
**階段**：1 — 設計

> 此處列出的所有實體已存在於資料庫與程式碼庫中。本功能無需執行任何資料庫遷移。本文件描述適用於出貨功能的標準模型。

---

## 實體：Order

**資料表**：`order`  
**套件**：`com.group.admin.entity.Order`  
**Mapper**：`com.group.admin.mapper.OrderMapper`

### 欄位（出貨相關子集）

| 欄位（snake_case） | Java 欄位 | 型別 | 可為 NULL | 說明 |
|---------------------|-----------|------|----------|-------------|
| `id` | `id` | `String` (UUID) | 否 | 主鍵 |
| `order_number` | `orderNumber` | `String` | 否 | 人類可讀的訂單編號 |
| `user_id` | `userId` | `String` (UUID) | 否 | FK → `user.id` |
| `store_id` | `storeId` | `String` (UUID) | 否 | FK → `store.id` |
| `total_items` | `totalItems` | `Integer` | 否 | 獎品項目數量 |
| `shipping_method` | `shippingMethod` | `String` | 是 | `ShippingMethodEnum` 代碼 |
| `status` | `status` | `String` | 否 | `OrderStatusEnum` 代碼 |
| `payment_status` | `paymentStatus` | `String` | 否 | 付款狀態代碼 |
| `recipient_name` | `recipientName` | `String` | 是 | 宅配收件人姓名 |
| `recipient_phone` | `recipientPhone` | `String` | 是 | 宅配收件人電話 |
| `recipient_address` | `recipientAddress` | `String` | 是 | 宅配地址 |
| `store_code` | `storeCode` | `String` | 是 | 超商分店代碼 |
| `store_name` | `storeName` | `String` | 是 | 超商分店名稱 |
| `store_address` | `storeAddress` | `String` | 是 | 超商分店地址 |
| `tracking_no` | `trackingNo` | `String` | 是 | 物流追蹤單號（管理員在 SHIPPED 時設定） |
| `remark` | `remark` | `String` | 是 | 自由文字備註 |
| `created_at` | `createdAt` | `LocalDateTime` | 否 | 訂單建立時間戳記 |
| `updated_at` | `updatedAt` | `LocalDateTime` | 否 | 最後修改時間戳記 |
| `shipped_at` | `shippedAt` | `LocalDateTime` | 是 | 狀態轉為 SHIPPED 的時間戳記 |
| `completed_at` | `completedAt` | `LocalDateTime` | 是 | 狀態轉為 COMPLETED 的時間戳記 |
| `cancelled_at` | `cancelledAt` | `LocalDateTime` | 是 | 狀態轉為 CANCELLED 的時間戳記 |
| `cancelled_by` | `cancelledBy` | `String` | 是 | 執行取消的管理員使用者 ID |
| `cancel_reason` | `cancelReason` | `String` | 是 | 自由文字取消原因 |

### 驗證規則（在服務 / DTO 層套用）

- 當 `shippingMethod = HOME_DELIVERY` 時：`recipientName`、`recipientPhone`、`recipientAddress` 必須非空白。
- 當 `shippingMethod = SEVEN_ELEVEN` 或 `FAMILY_MART` 時：`storeCode`、`storeName` 必須非空白。
- `shippingMethod` 必須為有效的 `ShippingMethodEnum` 代碼。
- `status` 必須為有效的 `OrderStatusEnum` 代碼。
- `trackingNo` 僅在管理員呼叫 `PUT /admin/orders/{id}/ship` 時為必填。

---

## 實體：OrderStatusLog

**資料表**：`order_status_log`  
**套件**：`com.group.admin.entity.OrderStatusLog`  
**Mapper**：`com.group.admin.mapper.OrderStatusLogMapper`

### 欄位

| 欄位 | Java 欄位 | 型別 | 可為 NULL | 說明 |
|--------|-----------|------|----------|-------------|
| `id` | `id` | `String` (UUID) | 否 | 主鍵 |
| `order_id` | `orderId` | `String` (UUID) | 否 | FK → `order.id` |
| `from_status` | `fromStatus` | `String` | 是 | 前一個 `OrderStatusEnum` 代碼（建立時為 null） |
| `to_status` | `toStatus` | `String` | 否 | 新的 `OrderStatusEnum` 代碼 |
| `operator_id` | `operatorId` | `String` (UUID) | 否 | 觸發變更的使用者 / 管理員 |
| `operator_type` | `operatorType` | `String` | 否 | `"ADMIN"` / `"STORE_OWNER"` / `"SYSTEM"` |
| `remark` | `remark` | `String` | 是 | 可選的情境備註 |
| `created_at` | `createdAt` | `LocalDateTime` | 否 | 日誌條目時間戳記 |

### 不變式
- 每次狀態轉換都會追加**一筆**日誌條目（從不更新、從不刪除）。
- FR-010 合規：完整的稽核追蹤，記錄誰在何時修改了什麼。

---

## 實體：ShipInfoReq（新 DTO — 非 DB 實體）

**套件**：`com.group.admin.req.order.ShipInfoReq`  
**使用於**：`POST /order/{orderId}/shipping-info`

### 欄位

| 欄位 | 型別 | 驗證 | 說明 |
|-------|------|-----------|-------------|
| `shippingMethod` | `String` | `@NotBlank` | 必須為有效的 `ShippingMethodEnum` 代碼 |
| `recipientName` | `String` | 條件式（HOME_DELIVERY 時必填） | 收件人全名 |
| `recipientPhone` | `String` | 條件式（HOME_DELIVERY 時必填） | 收件人電話號碼 |
| `recipientAddress` | `String` | 條件式（HOME_DELIVERY 時必填） | 完整配送地址 |
| `storeCode` | `String` | 條件式（SEVEN_ELEVEN / FAMILY_MART 時必填） | 分店代碼 |
| `storeName` | `String` | 條件式（SEVEN_ELEVEN / FAMILY_MART 時必填） | 分店名稱 |
| `storeAddress` | `String` | 可選 | 分店地址 |
| `remark` | `String` | 無 | 自由文字備註 |

---

## 狀態機：訂單出貨狀態

```
                         ┌─────────────────────────────┐
                         │         CANCELLED            │
                         │       (已取消)                │
                         └──────────────────────────────┘
                               ▲              ▲
                    cancel()   │              │ cancel()
                               │              │
┌───────────┐  prepare()  ┌────────────┐  ship()  ┌──────────┐  complete()  ┌───────────┐
│  PENDING  │────────────►│ PREPARING  │─────────►│ SHIPPED  │─────────────►│ COMPLETED │
│ (待處理)  │             │(準備出貨中) │          │ (已出貨) │              │ (已完成)  │
└───────────┘             └────────────┘          └──────────┘              └───────────┘
```

### 轉換規則

| 起始狀態 | 目標狀態 | 觸發端點 | 執行角色 | 守衛條件 |
|------|-----|---------|-------|-------|
| PENDING | PREPARING | `PUT /admin/orders/{id}/prepare` | ADMIN / STORE_OWNER | — |
| PREPARING | SHIPPED | `PUT /admin/orders/{id}/ship` | ADMIN / STORE_OWNER | 需要 `trackingNo` |
| SHIPPED | COMPLETED | `PUT /admin/orders/{id}/complete` | ADMIN / STORE_OWNER | — |
| PENDING | CANCELLED | `PUT /admin/orders/{id}/cancel` | 僅 ADMIN | `isCancellable() = true` |
| PREPARING | CANCELLED | `PUT /admin/orders/{id}/cancel` | 僅 ADMIN | `isCancellable() = true` |
| SHIPPED | CANCELLED | ❌ 拒絕 | — | `isCancellable() = false` |
| COMPLETED | CANCELLED | ❌ 拒絕 | — | `isFinished() = true` |
| 任意 | PENDING（逆向） | ❌ 拒絕 | — | 不允許逆向轉換 |

### 列舉對應

```java
// OrderStatusEnum (existing)
PENDING    → "待處理"      → initial state on order creation
PREPARING  → "準備出貨中"  → store confirms inventory
SHIPPED    → "已出貨"      → carrier tracking number recorded
COMPLETED  → "已完成"      → delivery confirmed / auto-completed
CANCELLED  → "已取消"      → cancelled before shipping

// ShippingMethodEnum (existing)
HOME_DELIVERY → "宅配到府"     → requires recipientName/Phone/Address
SEVEN_ELEVEN  → "7-11 取貨"   → requires storeCode/storeName
FAMILY_MART   → "全家取貨"    → requires storeCode/storeName
```

---

## 關聯關係

```
User (1) ──────────── (N) Order (1) ──────── (N) OrderStatusLog
                              │
                              └──── (N) OrderItem (1) ──── LotteryPrize
                              │
                           Store (1)
```

- **Order ↔ User**：每位使用者可有多筆訂單；`order.user_id` 為不可為 null 的 FK。
- **Order ↔ Store**：每間店可有多筆訂單；`order.store_id` 為不可為 null 的 FK。
- **Order ↔ OrderStatusLog**：一對多稽核日誌；不可變的僅附加模式。
- **Order ↔ OrderItem**：一對多；每個項目為一個中獎獎品。

---

## 無需資料庫變更

本功能所需的所有欄位已存在於 `order` 與 `order_status_log` 資料表中。功能 `002-express-shipping` 無需 DDL 腳本或 Flyway 遷移。
