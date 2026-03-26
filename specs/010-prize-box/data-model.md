# 資料模型： 獎品盒 (Prize Box) — Feature 010

**階段**：1 — 設計  
**日期**：2026-03-22  
**分支**：`010-prize-box`

---

## 實體概覽

```
User ─────────────────────────────────────────────────┐
                                                       │ userId
                                          PrizeBox ◄──┘
                                         /    |    \
                                        /     |     \
                              lotteryId  prizeId  storeId
                                  │         │        │
                               Lottery  LotteryPrize Store
                                   └──── (source) ────┘
                                                       │
                                               OrderItem (if SHIPPED)
                                                       │
                                                    Order
```

---

## 實體 1： PrizeBox（獎品盒項目）

**資料表**：`prize_box`  
**狀態**：已存在，需確認/補充欄位

### 欄位

| 欄位 | Java 型別 | 可空 | 說明 |
|--------|-----------|----------|-------------|
| `id` | `String` (UUID) | ❌ | PK，UUID v4 |
| `user_id` | `String` (UUID) | ❌ | FK → `user.id` |
| `lottery_id` | `String` (UUID) | ❌ | FK → `lottery.id`（來源一番賞） |
| `prize_id` | `String` (UUID) | ❌ | FK → `lottery_prize.id` |
| `store_id` | `String` (UUID) | ❌ | FK → `store.id` |
| `draw_result_id` | `String` (UUID) | ✅ | FK → 抽獎結果記錄（保留追蹤用） |
| `status` | `String` (enum) | ❌ | `IN_BOX` / `SHIPPED` / `RECYCLED` |
| `is_recyclable` | `Byte` (TINYINT) | ✅ | 覆蓋旗標（NULL = 由 recycleBonus 決定） |
| `is_shippable` | `Byte` (TINYINT) | ❌ | DEFAULT 1；若為 0 則不可出貨 |
| `recycle_bonus` | `Long` (BIGINT) | ✅ | 回收可得 Bonus 點數；0 = 不可回收 |
| `order_id` | `String` (UUID) | ✅ | FK → `order.id`（出貨後填入） |
| `shipped_at` | `LocalDateTime` | ✅ | 出貨時間 |
| `recycled_at` | `LocalDateTime` | ✅ | 回收時間 |
| `created_at` | `LocalDateTime` | ❌ | 獲獎時間（抽中時建立） |
| `updated_at` | `LocalDateTime` | ✅ | 最後更新時間 |

### 狀態轉換

```
        ┌──────────────────┐
        │      IN_BOX      │  ← 抽獎後自動建立
        └──────────────────┘
               │       │
    ship()     │       │  recycle()
               ▼       ▼
          SHIPPED    RECYCLED
          （不可逆）  （不可逆）
```

### 業務規則

- `IN_BOX` 是唯一允許執行 ship/recycle 的狀態
- `isRecyclable = (recycleBonus != null && recycleBonus > 0)`，除非 `is_recyclable` 欄位顯式覆蓋為 0
- `is_shippable = 1`（default），若店家/管理員設為 0 則前台不允許選取
- 回收為不可逆操作，無 undo 端點
- 出貨時自動按 `store_id` 拆單，每家店家建立一筆 `Order`

### 索引

```sql
INDEX idx_prize_box_user_status (user_id, status)   -- 獎品盒查詢
INDEX idx_prize_box_store (store_id)                 -- 店家分組
INDEX idx_prize_box_lottery (lottery_id)             -- 來源追蹤
```

### DDL (補充欄位)

```sql
-- 若 is_shippable 欄位不存在，需執行：
ALTER TABLE prize_box
  ADD COLUMN is_shippable TINYINT NOT NULL DEFAULT 1 COMMENT '是否可出貨：1=可, 0=不可' AFTER is_recyclable;

-- 確認 updated_at 欄位存在：
ALTER TABLE prize_box
  ADD COLUMN updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間';
```

---

## 實體 2： UserAddress（收件地址）

**資料表**：`user_address`  
**狀態**：已存在，無需修改

### 欄位

| 欄位 | Java 型別 | 可空 | 說明 |
|--------|-----------|----------|-------------|
| `id` | `String` (UUID) | ❌ | PK |
| `user_id` | `String` (UUID) | ❌ | FK → `user.id` |
| `recipient_name` | `String` | ❌ | 收件人姓名 |
| `recipient_phone` | `String` | ❌ | 收件人電話 |
| `postal_code` | `String` | ✅ | 郵遞區號 |
| `city` | `String` | ✅ | 縣市 |
| `district` | `String` | ✅ | 鄉鎮市區 |
| `address` | `String` | ❌ | 詳細地址 |
| `is_default` | `Boolean` | ❌ | 是否為預設地址 |
| `created_at` | `LocalDateTime` | ❌ | 建立時間 |
| `updated_at` | `LocalDateTime` | ✅ | 更新時間 |

### 在獎品盒功能中的用途

`PrizeBoxShipReq.userAddressId` → 查詢 `UserAddress` → 填入 `Order` 的收件人欄位。

---

## 關聯實體（本功能僅讀取）

### LotteryPrize（獎項）

**資料表**：`lottery_prize`  
**用途**：讀取 `name`, `image_url`, `level`, `prize_value`

| 欄位 | 說明 |
|--------|-------------|
| `id` | PK |
| `lottery_id` | FK → `lottery.id` |
| `name` | 獎項名稱 |
| `level` | 獎項等級（A/B/C/D/E/F/G/Last） |
| `image_url` | 獎項圖片 URL |
| `prize_value` | 獎品市值（Long，如有） |
| `recycle_bonus` | 回收 Bonus（此欄位設定後 複製到 PrizeBox.recycle_bonus） |

### Lottery（一番賞商品）

**資料表**：`lottery`  
**用途**：讀取 `title`, `image_url`

### Store（店家）

**資料表**：`store`  
**用途**：讀取 `store_name`, 檢查 `status`（已停用店家不得新建訂單）

---

## DTO 異動

### PrizeBoxItemRes — 新增欄位

```java
// 新增至現有 PrizeBoxItemRes
private Boolean isShippable;     // FR-011: 是否可出貨
private Long prizeValue;          // FR-002: 獎品市值
private LocalDateTime shippedAt;  // 出貨時間（history 用）
private LocalDateTime recycledAt; // 回收時間（history 用）
```

### PrizeBoxShipReq — 修正與新增

```java
// 修正：補上 import jakarta.validation.constraints.NotBlank;
// 新增選填欄位：
private String userAddressId;    // P3: 使用已儲存地址的 ID（選填）
```

### PrizeBoxSummaryRes — 現有欄位確認

```java
private String storeId;
private String storeName;
private Integer itemCount;
private List<PrizeBoxItemRes> items;
```

---

## 狀態機摘要

| 狀態 | 中文 | 可執行操作 | 觸發條件 |
|------|------|-----------|---------|
| `IN_BOX` | 在獎品盒中 | ship, recycle | 抽獎後自動建立 |
| `SHIPPED` | 已出貨 | 無 | `POST /prize-box/ship` 成功 |
| `RECYCLED` | 已回收 | 無 | `POST /prize-box/recycle` 成功 |
