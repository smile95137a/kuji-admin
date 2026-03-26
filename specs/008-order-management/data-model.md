# Data Model: 訂單管理 (Order Management)

**功能分支**：`008-order-management`  
**日期**：2026-03-22  
**階段**：1 — 設計

---

## 實體概覽

```
┌──────────────┐       ┌──────────────┐       ┌─────────────────────┐
│    order     │ 1───* │  order_item  │       │   order_status_log  │
│              │       │              │       │                     │
│ id (PK)      │       │ id (PK)      │       │ id (PK)             │
│ order_no     │       │ order_id(FK) │       │ order_id (FK)       │
│ user_id (FK) │       │ prize_box_id │       │ from_status         │
│ store_id(FK) │       │ lottery_id   │       │ to_status           │
│ status       │       │ prize_id     │       │ operator_id         │
│ ...          │       │ prize_name   │       │ operator_type       │
└──────────────┘       └──────────────┘       │ remark              │
       │                                       └─────────────────────┘
       │ * ─── 1
┌──────────────┐
│  prize_box   │  (existing; linked via order_item.prize_box_id)
│              │
│ id (PK)      │
│ status       │  IN_BOX | SHIPPED | RECYCLED
│ order_id(FK) │  ← set when order created; cleared on cancel
└──────────────┘
```

---

## 實體 1：`Order`

**Java 類別**：`com.group.admin.entity.Order`  
**資料表**：`` `order` ``（反引號跳脫——MySQL 保留字）

| 欄位 | Java 欄位 | 型別 | 限制 | 備註 |
|------|----------|------|------|------|
| `id` | `id` | `VARCHAR(36)` | PK, NOT NULL | UUID v4 |
| `order_no` | `orderNumber` | `VARCHAR(50)` | UNIQUE, NOT NULL | `ORD-{ts}-{rand6}` |
| `user_id` | `userId` | `VARCHAR(36)` | FK → `user.id`, NOT NULL | 建立訂單的玩家 |
| `store_id` | `storeId` | `VARCHAR(36)` | FK → `store.id`, NOT NULL | 負責履行訂單的店家 |
| `total_items` | `totalItems` | `INT` | NOT NULL | order_item 列數 |
| `shipping_method` | `shippingMethod` | `VARCHAR(20)` | NOT NULL | `HOME_DELIVERY` / `SEVEN_ELEVEN` / `FAMILY_MART` |
| `status` | `status` | `VARCHAR(20)` | NOT NULL, DEFAULT 'PENDING' | 參見下方狀態機 |
| `payment_status` | `paymentStatus` | `VARCHAR(20)` | nullable | v1 不使用（獎品在抽獎時已預付） |
| `recipient_name` | `recipientName` | `VARCHAR(100)` | NOT NULL | |
| `recipient_phone` | `recipientPhone` | `VARCHAR(20)` | NOT NULL | |
| `recipient_address` | `recipientAddress` | `VARCHAR(500)` | nullable | HOME_DELIVERY 時必填 |
| `store_code` | `storeCode` | `VARCHAR(20)` | nullable | 超商取貨代碼 |
| `store_name` | `storeName` | `VARCHAR(100)` | nullable | 超商名稱 |
| `store_address` | `storeAddress` | `VARCHAR(500)` | nullable | 超商地址 |
| `tracking_no` | `trackingNo` | `VARCHAR(100)` | nullable | 狀態轉為 SHIPPED 時設定 |
| `remark` | `remark` | `VARCHAR(500)` | nullable | 內部備註 |
| `created_at` | `createdAt` | `DATETIME` | NOT NULL, DEFAULT NOW() | |
| `updated_at` | `updatedAt` | `DATETIME` | ON UPDATE NOW() | |
| `shipped_at` | `shippedAt` | `DATETIME` | nullable | 狀態轉為 SHIPPED 時設定 |
| `completed_at` | `completedAt` | `DATETIME` | nullable | 狀態轉為 COMPLETED 時設定 |
| `cancelled_at` | `cancelledAt` | `DATETIME` | nullable | 狀態轉為 CANCELLED 時設定 |
| `cancelled_by` | `cancelledBy` | `VARCHAR(36)` | nullable | 執行取消的操作者 ID |
| `cancel_reason` | `cancelReason` | `VARCHAR(500)` | nullable | |

**索引**：
- `idx_user (user_id)`
- `idx_store (store_id)`
- `idx_status (status)`
- `idx_order_no (order_no)`
- `idx_created_at (created_at DESC)`

---

## 實體 2：`OrderItem`

**Java 類別**：`com.group.admin.entity.OrderItem`  
**資料表**：`order_item`

| 欄位 | Java 欄位 | 型別 | 限制 | 備註 |
|------|----------|------|------|------|
| `id` | `id` | `VARCHAR(36)` | PK | UUID v4 |
| `order_id` | `orderId` | `VARCHAR(36)` | FK → `order.id` ON DELETE CASCADE | |
| `prize_box_id` | `prizeBoxId` | `VARCHAR(36)` | FK → `prize_box.id` | 來源獎品盒列 |
| `lottery_id` | `lotteryId` | `VARCHAR(36)` | NOT NULL | 所屬商品／抽獎活動 |
| `lottery_title` | `lotteryTitle` | `VARCHAR(255)` | NOT NULL | 建立訂單時的快照 |
| `lottery_image_url` | `lotteryImageUrl` | `VARCHAR(500)` | nullable | 快照 |
| `prize_id` | `prizeId` | `VARCHAR(36)` | NOT NULL | |
| `prize_name` | `prizeName` | `VARCHAR(255)` | NOT NULL | 建立訂單時的快照 |
| `prize_image_url` | `prizeImageUrl` | `VARCHAR(500)` | nullable | 快照 |
| `prize_level` | `prizeLevel` | `VARCHAR(10)` | nullable | 例如："A"、"B"、"LAST" |
| `created_at` | `createdAt` | `DATETIME` | NOT NULL | |

**索引**：`idx_order (order_id)`、`idx_prize_box (prize_box_id)`

> **快照模式**：獎品名稱與圖片在建立訂單時複製，以確保後續的獎品修改不會影響歷史訂單顯示。

---

## 實體 3：`OrderStatusLog`

**Java 類別**：`com.group.admin.entity.OrderStatusLog`  
**資料表**：`order_status_log`

| 欄位 | Java 欄位 | 型別 | 限制 | 備註 |
|------|----------|------|------|------|
| `id` | `id` | `VARCHAR(36)` | PK | UUID v4 |
| `order_id` | `orderId` | `VARCHAR(36)` | FK → `order.id` ON DELETE CASCADE | |
| `from_status` | `fromStatus` | `VARCHAR(20)` | nullable | 初始 PENDING 建立時為 null |
| `to_status` | `toStatus` | `VARCHAR(20)` | NOT NULL | |
| `operator_id` | `operatorId` | `VARCHAR(36)` | nullable | 管理員使用者 ID 或玩家 ID |
| `operator_type` | `operatorType` | `VARCHAR(20)` | nullable | `ADMIN` / `STORE_OWNER` / `STORE_EDITOR` / `PLAYER` / `SYSTEM` |
| `remark` | `remark` | `VARCHAR(500)` | nullable | 取消原因、追蹤編號等 |
| `created_at` | `createdAt` | `DATETIME` | NOT NULL | |

**索引**：`idx_order (order_id)`、`idx_created_at (created_at DESC)`

---

## 狀態機

```
                    ┌─────────────┐
         [create]   │   PENDING   │
        ──────────► │  (待處理)    │
                    └──────┬──────┘
                           │ store advances
                    ┌──────▼──────┐
                    │  PREPARING  │
                    │  (備貨中)    │
                    └──────┬──────┘
                           │ store marks shipped
                    ┌──────▼──────┐
                    │   SHIPPED   │  ← Point of No Return
                    │  (已出貨)    │
                    └──────┬──────┘
                           │ auto or player confirms
                    ┌──────▼──────┐
                    │  COMPLETED  │
                    │  (已完成)    │
                    └─────────────┘

   CANCELLED (已取消) ← allowed only from PENDING or PREPARING
                      by ADMIN / STORE_OWNER / STORE_EDITOR only
```

**轉換表**：

| 來源 | 目標 | 允許角色 | 副作用 |
|------|------|---------|--------|
| — | `PENDING` | 系統（建立訂單） | `prize_box.status → SHIPPED`、`prize_box.order_id` 設定 |
| `PENDING` | `PREPARING` | STORE_OWNER、STORE_EDITOR、ADMIN | 新增日誌記錄 |
| `PREPARING` | `SHIPPED` | STORE_OWNER、STORE_EDITOR、ADMIN | 設定 `order.shipped_at`、`order.tracking_no`；新增日誌記錄 |
| `SHIPPED` | `COMPLETED` | STORE_OWNER、STORE_EDITOR、ADMIN、SYSTEM | 設定 `order.completed_at`；新增日誌記錄 |
| `PENDING` → `CANCELLED` | STORE_OWNER、STORE_EDITOR、ADMIN | `prize_box.status → IN_BOX`、`prize_box.order_id → NULL`；新增日誌記錄；**不退還點數** |
| `PREPARING` → `CANCELLED` | STORE_OWNER、STORE_EDITOR、ADMIN | 同上 |
| `SHIPPED` → 任意 | ❌ 禁止 | — | HTTP 409 |
| 任意 → 後退 | ❌ 禁止 | — | HTTP 409 |

---

## 驗證規則

### 建立訂單（`CreateOrderReq`）
- `prizeBoxIds`：非空清單；所有項目必須存在、屬於請求的玩家，且 `status = IN_BOX`
- `shippingMethod`：三個有效代碼之一：`HOME_DELIVERY`、`SEVEN_ELEVEN`、`FAMILY_MART`
- 若為 `HOME_DELIVERY`：`recipientAddress` 必填
- 若為 `SEVEN_ELEVEN` 或 `FAMILY_MART`：`storeCode` + `storeName` + `storeAddress` 必填
- `recipientName`：1-100 字元
- `recipientPhone`：台灣手機格式 `09\d{8}` 或市話 `0\d{1,2}-?\d{6,8}`
- 目標店家必須 `status = ACTIVE`

### 更新狀態（`UpdateOrderStatusReq`）
- `targetStatus`：必須是當前狀態的直接下一個狀態（`ordinal + 1`）
- 若 `targetStatus = SHIPPED`：建議填入 `trackingNo`（nullable，但會記錄）
- 呼叫者必須有權存取訂單所屬店家

### 取消（`CancelOrderReq`）
- `cancelReason`：可選文字（最多 500 字元）
- 訂單狀態必須為 `PENDING` 或 `PREPARING`
- 呼叫者角色：`ADMIN`、`STORE_OWNER` 或 `STORE_EDITOR`
- `STORE_OWNER`／`STORE_EDITOR` 須驗證店家所有權

---

## 相關實體（唯讀參照）

| 實體 | 資料表 | 用途 |
|------|-------|------|
| `User` | `user` | 訂單詳情中顯示玩家資訊（用戶名、電話） |
| `Store` | `store` | 訂單中顯示店家名稱；建立時驗證 status |
| `PrizeBox` | `prize_box` | 建立訂單時扣除；取消時還原 |
| `Prize` | `prize` | 名稱／圖片快照複製至 `order_item` |
| `Lottery` | `lottery` | 標題／圖片快照複製至 `order_item` |

---

## DTO 摘要

### 請求 DTO

| 類別 | 位置 | 欄位 |
|------|------|------|
| `CreateOrderReq` | `req/order/` | `prizeBoxIds: List<String>`、`shippingMethod`、`recipientName`、`recipientPhone`、`recipientAddress?`、`storeCode?`、`storeName?`、`storeAddress?` |
| `UpdateOrderStatusReq` | `req/order/` | `targetStatus: String`、`trackingNo?: String`、`remark?: String` |
| `CancelOrderReq` | `req/order/` | `cancelReason?: String` |

### 回應 DTO

| 類別 | 位置 | 用途 |
|------|------|------|
| `OrderRes` | `res/order/` | 列表檢視：`id`、`orderNo`、`status`、`totalItems`、`shippingMethod`、`storeName`、`playerName`、`createdAt` |
| `OrderDetailRes` | `res/order/` | 詳情檢視：`OrderRes` 所有欄位 + `items: List<OrderItemRes>`、`statusHistory: List<StatusLogRes>`、完整地址、`trackingNo`、`remark` |
| `OrderItemRes` | `res/order/` | 嵌套在 `OrderDetailRes`：`id`、`prizeName`、`prizeImageUrl`、`prizeLevel`、`lotteryTitle` |
| `StatusLogRes` | `res/order/` | 嵌套在 `OrderDetailRes`：`fromStatus`、`toStatus`、`operatorType`、`remark`、`createdAt` |

### 查詢條件

| 類別 | 位置 | 欄位 |
|------|------|------|
| `OrderCondition` | `condition/` | `storeId?`、`userId?`、`status?`、`shippingMethod?`、`startDate?`、`endDate?`、`orderNo?` |
