# 資料模型：抽獎票券系統（雙號碼與刮刮樂機制）

**功能**: 005-lottery-ticket-system  
**階段**: 1 — 設計與合約  
**日期**: 2026-03-22

---

## 概觀

本功能的核心由兩個實體支撐。兩者均已存在於程式庫中；本文件記錄其規範結構、欄位語義、約束條件，以及管理其生命週期的狀態機。

---

## 實體 1 — `LotteryTicket`

**資料表**: `lottery_ticket`  
**套件**: `com.group.admin.entity.LotteryTicket`

### 欄位

| 欄位名稱 | Java 欄位 | 類型 | 可為 NULL | 說明 |
|---|---|---|---|---|
| `id` | `id` | `VARCHAR(36)` | NOT NULL PK | 插入時生成的 UUID v4 |
| `lottery_id` | `lotteryId` | `VARCHAR(36)` | NOT NULL FK→`lottery.id` | 父抽獎活動 |
| `ticket_number` | `ticketNumber` | `INT` | NOT NULL | 順序實體格子：1…N。玩家可見標籤。 |
| `revealed_number` | `revealedNumber` | `INT` | NULL | 僅限 SCRATCH 模式。獨立洗牌的 1…N。DRAWN 前對前端隱藏。 |
| `prize_id` | `prizeId` | `VARCHAR(36)` | NULL FK→`lottery_prize.id` | 預分配獎品。NULL = 「謝謝惠顧」或待指定。 |
| `prize_level` | `prizeLevel` | `VARCHAR(20)` | NULL | 反正規化獎品等級（A/B/C/…/LAST）。 |
| `status` | `status` | `VARCHAR(20)` | NOT NULL DEFAULT `'AVAILABLE'` | 票券狀態。見下方狀態機。 |
| `drawn_by` | `drawnBy` | `VARCHAR(36)` | NULL FK→`user.id` | 抽取此票券的使用者。 |
| `drawn_at` | `drawnAt` | `DATETIME` | NULL | 抽獎時間戳記。 |
| `is_designated_prize` | `isDesignatedPrize` | `TINYINT(1)` | NOT NULL DEFAULT `0` | 1 = 大獎位置。 |
| `designated_by` | `designatedBy` | `VARCHAR(10)` | NULL | `'STORE'` / `'PLAYER'` / NULL。 |
| `created_at` | `createdAt` | `DATETIME` | NOT NULL | 列建立時間戳記。 |
| `updated_at` | `updatedAt` | `DATETIME` | NOT NULL | 最後修改時間戳記。 |

### 唯一約束

```sql
UNIQUE KEY uq_ticket_number  (lottery_id, ticket_number)
UNIQUE KEY uq_revealed_number (lottery_id, revealed_number)   -- SCRATCH modes; NULL safe with partial index
```

### 索引

```sql
INDEX idx_lottery_status (lottery_id, status)           -- hot path: fetch AVAILABLE tickets
INDEX idx_lottery_revealed (lottery_id, revealed_number) -- designation lookup
```

### 狀態機

```
          CREATE
            │
            ▼
        AVAILABLE ──── draw(ticketNumber) ──►  DRAWN
            │                                    (terminal)
            │  [future: admin cancel]
            ▼
          LOCKED  ──── unlock ──►  AVAILABLE
```

| 狀態 | 含義 | 狀態轉換 |
|---|---|---|
| `AVAILABLE` | 票券存在，尚未抽出 | → `DRAWN`（玩家抽獎），→ `LOCKED`（管理員保留） |
| `DRAWN` | 已永久抽出；獎品已揭露 | 終態 — 無後續轉換 |
| `LOCKED` | 保留 / 管理員保留 | → `AVAILABLE`（解鎖） |

### 驗證規則

- `ticket_number` ∈ [1, lottery.totalDraws]。在同一抽獎活動內必須唯一。
- `revealed_number` 設定後 ∈ [1, lottery.totalDraws]。在同一抽獎活動內必須唯一。
- `ticket_number` 與 `revealed_number` 必須由獨立洗牌分配（FR-004）。
- 在指定完成前，AVAILABLE SCRATCH 票券的 `prize_id` 為 NULL（FR-007, FR-008）。
- 一旦 `status = DRAWN`，`ticket_number`、`prize_id`、`revealed_number` 均不可修改。

### 前端可見性規則（FR-005, FR-006）

| 欄位 | AVAILABLE | DRAWN |
|---|---|---|
| `ticketNumber` | ✅ 可見 | ✅ 可見 |
| `status` | ✅ 可見 | ✅ 可見 |
| `revealedNumber` | ❌ 隱藏 | ✅ 可見（SCRATCH 模式） |
| `prizeId` | ❌ 隱藏 | ✅ 可見 |
| `prizeLevel` | ❌ 隱藏 | ✅ 可見 |
| `prizeName` | ❌ 隱藏 | ✅ 可見 |
| `prizeImageUrl` | ❌ 隱藏 | ✅ 可見 |
| `isDesignatedPrize` | ❌ 隱藏 | ✅ 可見 |
| `drawnBy` | ❌ 隱藏 | ✅ 可見 |
| `drawnAt` | ❌ 隱藏 | ✅ 可見 |

---

## 實體 2 — `LotterySession`（開套回合）

**資料表**: `lottery_session`  
**套件**: `com.group.admin.entity.LotterySession`

`LotterySession` 追蹤非 RANDOM 模式抽獎活動的單一**開套回合**。當任何玩家第一次從沒有當前 ACTIVE Session 的抽獎活動抽獎時建立。建立的玩家成為**開套玩家**。

### 欄位

| 欄位名稱 | Java 欄位 | 類型 | 可為 NULL | 說明 |
|---|---|---|---|---|
| `id` | `id` | `VARCHAR(36)` | NOT NULL PK | UUID v4 |
| `lottery_id` | `lotteryId` | `VARCHAR(36)` | NOT NULL FK→`lottery.id` | 父抽獎活動 |
| `opener_user_id` | `openerUserId` | `VARCHAR(36)` | NOT NULL FK→`user.id` | 開套玩家（本回合第一位抽獎者） |
| `protection_draws` | `protectionDraws` | `INT` | NOT NULL | 開套玩家保護涵蓋的抽獎次數（來自 `lottery.protectionDraws`） |
| `protection_start_time` | `protectionStartTime` | `DATETIME` | NULL | 開套玩家第一次抽獎時設定 |
| `protection_end_time` | `protectionEndTime` | `DATETIME` | NULL | `protectionStartTime` + `lottery.protectionMinutes` |
| `opener_draw_count` | `openerDrawCount` | `INT` | NOT NULL DEFAULT `0` | 開套玩家在本 Session 中的抽獎次數 |
| `opener_total_cost` | `openerTotalCost` | `BIGINT` | NOT NULL DEFAULT `0` | 開套玩家花費的總金幣（用於免費抽獎退款計算） |
| `free_draw_enabled` | `freeDrawEnabled` | `TINYINT(1)` | NOT NULL DEFAULT `0` | Session 建立時從 `lottery.freeDrawEnabled` 複製 |
| `free_draw_triggered` | `freeDrawTriggered` | `TINYINT(1)` | NOT NULL DEFAULT `0` | 1 = 退款已發放；防止雙重退款 |
| `free_draw_refund_amount` | `freeDrawRefundAmount` | `BIGINT` | NULL | 退款金額 |
| `free_draw_triggered_at` | `freeDrawTriggeredAt` | `DATETIME` | NULL | 退款時間戳記 |
| `free_draw_prize_id` | `freeDrawPrizeId` | `VARCHAR(36)` | NULL | 觸發免費抽獎的獎品 |
| `player_designated_numbers` | `playerDesignatedNumbers` | `TEXT` | NULL | 開套玩家指定的 `revealedNumber` 整數 JSON 陣列（僅限 SCRATCH_PLAYER）。NULL = 尚未指定。 |
| `status` | `status` | `VARCHAR(20)` | NOT NULL DEFAULT `'ACTIVE'` | Session 狀態 |
| `completed_at` | `completedAt` | `DATETIME` | NULL | Session 轉換為 COMPLETED 的時間 |
| `created_at` | `createdAt` | `DATETIME` | NOT NULL | |
| `updated_at` | `updatedAt` | `DATETIME` | NOT NULL | |

### 狀態機

```
         INSERT (first draw)
               │
               ▼
            ACTIVE
           /      \
          │        │
          │  protectionEndTime expires
          │  OR all tickets drawn       OR  admin cancel
          ▼        ▼                         ▼
      COMPLETED  EXPIRED                  CANCELLED
     (terminal) (terminal)               (terminal)
```

| 狀態 | 含義 |
|---|---|
| `ACTIVE` | 開套回合進行中；保護視窗可能開啟或已關閉 |
| `EXPIRED` | 保護視窗在抽獎活動完成前結束；下次抽獎建立新 Session |
| `COMPLETED` | 抽獎活動所有票券已抽出；Session 關閉 |
| `CANCELLED` | 管理員介入 |

### Session 生命週期（SCRATCH_PLAYER 閘門）

```
No active session
    └─► Player A draws
           └─► acquire sessionLocks[lotteryId]
                 └─► re-check: still no session
                       └─► INSERT session (openerUserId=A, playerDesignatedNumbers=NULL)
                             └─► release lock
                                   └─► Response: HTTP 202  requiresDesignation=true

Player A calls POST /designate
    └─► UPDATE session: playerDesignatedNumbers=[23,45], auto-assign remaining prizes
          └─► Designation complete → subsequent draws proceed normally

Player B draws while playerDesignatedNumbers IS NULL
    └─► HTTP 423 Locked (waiting for opener designation)
```

### 免費抽獎觸發條件（FR-011）

```
checkAndTriggerFreeDraw(sessionId, prizeId):
  1. session.freeDrawEnabled == 1
  2. session.freeDrawTriggered == 0        ← prevents double-refund
  3. session.openerDrawCount <= session.protectionDraws
  4. prize.isGrandPrize == 1
  5. caller == session.openerUserId        ← opener-only
  → refundAmount = session.openerTotalCost
  → walletService.addGold(openerUserId, refundAmount)
  → session.freeDrawTriggered = 1
```

### 開套玩家並發鎖（依 lotteryId）

```java
// In LotteryTicketServiceImpl
private final ConcurrentHashMap<String, Object> sessionLocks = new ConcurrentHashMap<>();

public LotterySession getOrCreateSession(String lotteryId, String userId) {
    Object lock = sessionLocks.computeIfAbsent(lotteryId, k -> new Object());
    synchronized (lock) {
        LotterySession existing = findActiveSession(lotteryId);
        if (existing != null) return existing;
        return createNewSession(lotteryId, userId);
    }
}
```

---

## 相關實體（現有 — 參考用）

### `Lottery`（父實體）

與本功能相關的關鍵欄位：

| 欄位 | 類型 | 用途 |
|---|---|---|
| `gameMode` | `VARCHAR(20)` | `'RANDOM'` / `'SCRATCH_STORE'` / `'SCRATCH_PLAYER'` / `'GACHA'` |
| `totalDraws` | `INT` | 票券總數 N |
| `protectionDraws` | `INT` | 開套玩家保護抽獎次數 |
| `protectionMinutes` | `INT` | 保護視窗持續時間（分鐘） |
| `freeDrawEnabled` | `TINYINT(1)` | 是否啟用免費抽獎退款 |
| `designatedPrizeNumbers` | `TEXT` | SCRATCH_STORE 大獎的 `revealedNumbers` JSON 陣列 |
| `ticketsGenerated` | `TINYINT(1)` | 標誌：1 = `generateTickets()` 已呼叫 |

### `LotteryPrize`（獎品池）

| 欄位 | 類型 | 用途 |
|---|---|---|
| `id` | `VARCHAR(36)` | UUID |
| `lotteryId` | `VARCHAR(36)` | FK |
| `name` | `VARCHAR(100)` | 顯示名稱 |
| `level` | `VARCHAR(20)` | 獎品等級（A/B/C/LAST） |
| `isGrandPrize` | `TINYINT(1)` | 1 = 大獎（觸發免費抽獎） |
| `quantity` | `INT` | 獎品池中的總數量 |
| `remaining` | `INT` | 每次抽獎遞減 |

---

## 模式 × 實體關聯矩陣

| gameMode | ticket_number 用途 | revealed_number 用途 | 獎品預分配 | 指定觸發 |
|---|---|---|---|---|
| `RANDOM` | 玩家依號碼選取 | NULL | 在 `generateTickets()` 時洗牌 | 不適用 |
| `SCRATCH_STORE` | 玩家依號碼選取 | 抽出前隱藏 | 店家在建立時透過 `designatedPrizeNumbers` 指定大獎；其餘自動分配 | 在抽獎活動建立時（`designatedPrizeNumbers`） |
| `SCRATCH_PLAYER` | 玩家依號碼選取 | 抽出前隱藏 | NULL 直到開套玩家指定；指定後其餘自動分配 | 開套玩家呼叫 `POST /designate` 後 |

---

## 資料庫遷移說明

不需要 DDL 變更。上述所有欄位經程式碼檢查確認已存在於生產結構中。以下索引**應驗證**是否存在於實際結構中：

```sql
-- Verify these exist; add if missing
SHOW INDEX FROM lottery_ticket WHERE Key_name IN ('idx_lottery_status', 'idx_lottery_revealed');
SHOW INDEX FROM lottery_session WHERE Key_name = 'idx_session_lottery_status';
```

若缺少則建議新增：

```sql
ALTER TABLE lottery_ticket
  ADD INDEX idx_lottery_status   (lottery_id, status),
  ADD INDEX idx_lottery_revealed (lottery_id, revealed_number);

ALTER TABLE lottery_session
  ADD INDEX idx_session_lottery_status (lottery_id, status);
```
