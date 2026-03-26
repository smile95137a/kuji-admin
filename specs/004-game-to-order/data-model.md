# 資料模型：遊戲至訂單流程（Game-to-Order Flow）

**功能**：`004-game-to-order`  
**日期**：2026-03-22  
**狀態**：已對照現有程式碼庫驗證

---

## 實體概覽

```
LotteryDrawRecord ──────────────────────────────────────────────────────┐
       │ (1)                                                              │
       │ drawResultId FK                                                  │
       ↓ (0..N)                                                          │
   PrizeBox  ←── userId (User FK) ←── storeId (Store FK)               │
       │  status: IN_BOX → SHIPPED / RECYCLED                            │
       │  orderId FK (set when SHIPPED)                                   │
       ↓ (N..1) via prizeBoxId                                           │
   OrderItem ──────────── orderId FK ──────────────────────────────────→ Order
                                                                          │ storeId FK
                                                                          │ userId FK
                                                                          │ status: PENDING → … → COMPLETED / CANCELLED
```

---

## 實體：PrizeBox

**資料表**：`prize_box`  
**套件**：`com.group.admin.entity.PrizeBox`  
**狀態**：✅ EXISTS

### 欄位

| 欄位 | 類型 | 可為 NULL | 說明 |
|-------|------|----------|-------------|
| `id` | `VARCHAR(36)` | NOT NULL PK | UUID — 由 service 產生 |
| `userId` | `VARCHAR(36)` | NOT NULL | FK → `user.id` — 擁有者 |
| `lotteryId` | `VARCHAR(36)` | NOT NULL | FK → `lottery.id` — 來源抽獎 |
| `prizeId` | `VARCHAR(36)` | NOT NULL | FK → `prize.id` — 獎品 |
| `storeId` | `VARCHAR(36)` | NOT NULL | FK → `store.id` — 所屬店家（用於訂單分單） |
| `drawResultId` | `VARCHAR(36)` | YES | FK → `lottery_draw_record.id` — 來源抽獎記錄 |
| `status` | `VARCHAR(20)` | NOT NULL | Enum：`IN_BOX` / `SHIPPED` / `RECYCLED` |
| `isRecyclable` | `TINYINT(1)` | NOT NULL | 此獎品是否可兌換 |
| `recycleBonus` | `BIGINT` | YES | 兌換後獲得的積分 |
| `recycledAt` | `DATETIME` | YES | 兌換時間戳記 |
| `orderId` | `VARCHAR(36)` | YES | FK → `order.id` — 狀態為 `SHIPPED` 時設定 |
| `shippedAt` | `DATETIME` | YES | 出貨時間戳記 |
| `createdAt` | `DATETIME` | NOT NULL | 插入時間戳記 |
| `updatedAt` | `DATETIME` | NOT NULL | 最後更新時間戳記 |

### 狀態機

```
         draw()
           │
           ▼
        IN_BOX ────── shipPrizes() ──────► SHIPPED
           │                                   │
           │◄────── cancelOrder() ─────────────┘
           │
           └────── recyclePrizes() ──────► RECYCLED
```

### 驗證規則

- 呼叫 `shipPrizes()` 前，`status` 必須為 `IN_BOX`（否則拋出 `BusinessException`）
- `PrizeBox` 上的 `userId` 必須與已認證使用者相符（`PrizeBoxServiceImpl` 中的擁有者檢查）
- 一旦狀態為 `SHIPPED` 或 `RECYCLED`，記錄不可刪除（FR-007）
- `status = SHIPPED` 的 `PrizeBox` 必須設定 `orderId`（不可為 null）

### Enum：PrizeBoxStatusEnum

```java
// com.group.admin.enums.PrizeBoxStatusEnum
IN_BOX("IN_BOX",     "在獎品盒中")   // Default — prize is in the box, ready to ship
SHIPPED("SHIPPED",   "已出貨")        // Linked to an Order, shipped
RECYCLED("RECYCLED", "已回收")        // Recycled for bonus points
```

---

## 實體：Order

**資料表**：`order`  
**套件**：`com.group.admin.entity.Order`  
**狀態**：✅ EXISTS

### 欄位

| 欄位 | 類型 | 可為 NULL | 說明 |
|-------|------|----------|-------------|
| `id` | `VARCHAR(36)` | NOT NULL PK | UUID |
| `orderNumber` | `VARCHAR(50)` | NOT NULL UNIQUE | 人類可讀的訂單編號 |
| `userId` | `VARCHAR(36)` | NOT NULL | FK → `user.id` |
| `storeId` | `VARCHAR(36)` | NOT NULL | FK → `store.id` — 每筆訂單對應一間店家 |
| `totalItems` | `INT` | NOT NULL | OrderItem 記錄數量 |
| `shippingMethod` | `VARCHAR(30)` | NOT NULL | `HOME_DELIVERY` / `SEVEN_ELEVEN` / `FAMILY_MART` |
| `status` | `VARCHAR(20)` | NOT NULL | 參見下方 OrderStatusEnum |
| `paymentStatus` | `VARCHAR(20)` | YES | 付款追蹤（保留欄位） |
| `recipientName` | `VARCHAR(100)` | NOT NULL | 若未提供，自動從使用者資料填入 |
| `recipientPhone` | `VARCHAR(20)` | NOT NULL | 若未提供，自動從使用者資料填入 |
| `recipientAddress` | `VARCHAR(255)` | YES | `HOME_DELIVERY` 時必填 |
| `storeCode` | `VARCHAR(50)` | YES | 超商門市代碼（SEVEN_ELEVEN / FAMILY_MART 使用） |
| `storeName` | `VARCHAR(100)` | YES | 超商門市名稱 |
| `storeAddress` | `VARCHAR(255)` | YES | 超商門市地址 |
| `trackingNo` | `VARCHAR(100)` | YES | 物流追蹤號碼（由管理員設定） |
| `remark` | `VARCHAR(500)` | YES | 玩家備註 |
| `createdAt` | `DATETIME` | NOT NULL | |
| `updatedAt` | `DATETIME` | NOT NULL | |
| `shippedAt` | `DATETIME` | YES | 管理員標記為 SHIPPED 的時間 |
| `completedAt` | `DATETIME` | YES | 管理員標記為 COMPLETED 的時間 |
| `cancelledAt` | `DATETIME` | YES | 取消時間 |
| `cancelledBy` | `VARCHAR(36)` | YES | 執行取消的使用者或管理員 ID |
| `cancelReason` | `VARCHAR(500)` | YES | 取消原因 |

### 狀態機

```
  createOrdersFromPrizeBox()
           │
           ▼
        PENDING ──── 管理員操作 ───► PREPARING ──── 管理員操作 ───► SHIPPED ───► COMPLETED
           │
           └── cancelOrder() ──► CANCELLED
                                     │
                                     └── (觸發 PrizeBox 重置為 IN_BOX)
```

### Enum：OrderStatusEnum

```java
// com.group.admin.enums.OrderStatusEnum
PENDING("PENDING",       "待處理")
PREPARING("PREPARING",   "準備出貨中")
SHIPPED("SHIPPED",       "已出貨")
COMPLETED("COMPLETED",   "已完成")
CANCELLED("CANCELLED",   "已取消")
```

### Enum：ShippingMethodEnum

```java
HOME_DELIVERY("HOME_DELIVERY", "宅配")
SEVEN_ELEVEN("SEVEN_ELEVEN",   "7-11 取貨")
FAMILY_MART("FAMILY_MART",     "全家取貨")
```

### 驗證規則

- `shippingMethod = HOME_DELIVERY` 需要 `recipientAddress` 非空白
- `shippingMethod = SEVEN_ELEVEN / FAMILY_MART` 需要 `storeCode` + `storeName` 非空白
- `recipientName` 和 `recipientPhone` 若未提供，自動從 `User` 資料填入；自動填入後仍為空白時拋出 `BusinessException`
- 只有 `PENDING` 狀態的訂單可被玩家取消（管理員可取消任何狀態）

---

## 實體：OrderItem

**資料表**：`order_item`  
**套件**：`com.group.admin.entity.OrderItem`  
**狀態**：✅ EXISTS

### 欄位

| 欄位 | 類型 | 可為 NULL | 說明 |
|-------|------|----------|-------------|
| `id` | `VARCHAR(36)` | NOT NULL PK | UUID |
| `orderId` | `VARCHAR(36)` | NOT NULL | FK → `order.id` |
| `prizeBoxId` | `VARCHAR(36)` | NOT NULL | FK → `prize_box.id` — 連結回來源 |
| `lotteryId` | `VARCHAR(36)` | YES | 非正規化，用於顯示 |
| `lotteryTitle` | `VARCHAR(200)` | YES | 非正規化的抽獎標題 |
| `lotteryImageUrl` | `VARCHAR(500)` | YES | 非正規化的抽獎圖片 URL |
| `prizeId` | `VARCHAR(36)` | YES | 非正規化的獎品 ID |
| `prizeName` | `VARCHAR(200)` | YES | 訂單建立時的非正規化獎品名稱 |
| `prizeGrade` | `VARCHAR(50)` | YES | 非正規化的獎品等級 |
| `prizeImage` | `VARCHAR(500)` | YES | 非正規化的獎品圖片 |
| `prizeImageUrl` | `VARCHAR(500)` | YES | 完整 S3 URL |
| `prizeLevel` | `VARCHAR(50)` | YES | 獎品等級標籤 |
| `createdAt` | `DATETIME` | NOT NULL | |

### 設計說明

- 獎品名稱 / 圖片 / 等級在訂單建立時**非正規化**儲存。即使獎品目錄後來變更，訂單歷史仍能保持穩定。
- `prizeBoxId` 是連結回原始 `PrizeBox` 記錄的橋樑 — 在訂單取消時用來重置獎品盒狀態。

---

## 實體：LotteryDrawRecord

**資料表**：`lottery_draw_record`  
**套件**：`com.group.admin.entity.LotteryDrawRecord`  
**狀態**：✅ EXISTS（從本功能角度為唯讀）

### 相關欄位

| 欄位 | 類型 | 說明 |
|-------|------|-------------|
| `id` | `VARCHAR(36)` | PK — 被 `PrizeBox.drawResultId` 參照 |
| `userId` | `VARCHAR(36)` | 抽獎者 |
| `lotteryId` | `VARCHAR(36)` | 抽獎的彩券 |
| `prizeId` | `VARCHAR(36)` | 獲得的獎品 |
| `status` | `VARCHAR(20)` | 抽獎狀態 |
| `createdAt` | `DATETIME` | 抽獎時間 |

---

## 回應 DTO

### PrizeBoxItemRes

```java
// com.group.admin.res.prizebox.PrizeBoxItemRes
@Data @Builder
public class PrizeBoxItemRes {
    String id;               // PrizeBox UUID
    String userId;
    String lotteryId;
    String lotteryTitle;
    String lotteryImageUrl;
    String prizeId;
    String prizeName;
    String prizeLevel;
    String prizeImageUrl;
    String storeId;
    String storeName;
    String status;           // "IN_BOX" | "SHIPPED" | "RECYCLED"
    String statusName;       // "在獎品盒中" | "已出貨" | "已回收"
    Boolean isRecyclable;
    Long recycleBonus;
    LocalDateTime createdAt;
}
```

### OrderDetailRes（摘要）

```java
// com.group.admin.res.order.OrderDetailRes
@Data @Builder
public class OrderDetailRes {
    String id;
    String orderNo;          // Human-readable order number
    String userId;
    String storeId;
    String storeName;
    Integer totalItems;
    String shippingMethod;
    String shippingStatus;   // OrderStatusEnum code
    String shippingStatusName;
    String recipientName;
    String recipientPhone;
    String recipientAddress;
    String storeCode;
    String storeName;
    String trackingNo;
    String remark;
    LocalDateTime createdAt;
    LocalDateTime shippedAt;
    LocalDateTime completedAt;
    LocalDateTime cancelledAt;
    String cancelReason;
    List<OrderItemRes> items;
}
```

---

## 關聯關係摘要

| 來源 | 關係 | 目標 | 關聯欄位 |
|------|-----------|----|-----------|
| `LotteryDrawRecord` | 1 → 0..N | `PrizeBox` | `PrizeBox.drawResultId` |
| `User` | 1 → 0..N | `PrizeBox` | `PrizeBox.userId` |
| `Store` | 1 → 0..N | `PrizeBox` | `PrizeBox.storeId` |
| `PrizeBox` | N → 1 | `Order` | `PrizeBox.orderId`（SHIPPED 前為 nullable） |
| `Order` | 1 → 1..N | `OrderItem` | `OrderItem.orderId` |
| `OrderItem` | N → 1 | `PrizeBox` | `OrderItem.prizeBoxId` |
| `User` | 1 → 0..N | `Order` | `Order.userId` |
| `Store` | 1 → 0..N | `Order` | `Order.storeId` |
