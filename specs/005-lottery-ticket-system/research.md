# 研究分析：抽獎票券系統（雙號碼與刮刮樂機制）

**功能**: 005-lottery-ticket-system  
**階段**: 0 — 輪廓與研究  
**日期**: 2026-03-22

---

## 1. 雙號碼洗牌演算法

### 決策
使用兩個**獨立的** Fisher-Yates 洗牌 — 一個用於 `ticket_number`→`prize_id` 對應（RANDOM 模式），另一個用於 `revealed_number` 排列（SCRATCH 模式）。兩個洗牌均以 `java.security.SecureRandom` 作為種子。

### 理由
- `ticket_number` 是玩家選擇的實體格子標籤（1…N）。它絕不能預測分配了哪個獎品。
- `revealed_number` 是印在刮刮塗層下方的號碼。它獨立隨機化，即使玩家知道所有 `ticket_number` 值，也無法推斷 `revealed_number` 序列。
- 獨立性（FR-004）的保證方式：建立兩個獨立的洗牌列表，僅在插入時進行壓縮配對 — 兩個陣列在分配前**絕不**排序或配對。

### 演算法（generateScratchTickets）

```
tickets: ticket_number[1..N] → fixed sequential
revealedPool: [1..N] shuffled with SecureRandom.shuffle()

For i in 1..N:
  ticket[i].ticket_number   = i
  ticket[i].revealed_number = revealedPool[i]   // independent
  ticket[i].prize_id        = null              // assigned later
  ticket[i].status          = AVAILABLE
```

大獎分配（SCRATCH_STORE）：店家提供一組 `revealed_numbers` → 標記對應票券 `is_designated_prize = 1`。  
非大獎自動分配（FR-009）：剩餘獎品洗牌後在單次批次 UPDATE 中分配給非大獎票券。

### 已考慮的替代方案
| 替代方案 | 棄用原因 |
|---|---|
| `Math.random()` / `Random` | 非密碼學安全；在已知種子下可預測 |
| 單一洗牌（ticket_number == revealed_number） | 違反 FR-004 獨立性要求 |
| 在抽獎時依 DB 權重預先計算獎品對應 | 在並發下太慢；不支援基於票券的審計 |

---

## 2. 安全亂數 — Java SecureRandom 最佳實踐

### 決策
使用 `SecureRandom`（Linux 上預設為 `NativePRNG`），透過每個 Service bean 的 `new SecureRandom()` 取得一次。**不要**在每次請求時重新初始化種子。

### 理由
- Linux 上的 `SecureRandom` 在初始種子化後從 `/dev/urandom` 讀取，提供非阻塞的 CSPRNG 輸出。
- 每個 Service bean 單一實例是執行緒安全的；JDK 同步內部狀態。
- 每次請求重新實例化 `SecureRandom` 是不必要的開銷，且在 Linux 熵池較低時可能短暫阻塞。

### 實作模式

```java
@Service
public class LotteryTicketServiceImpl implements LotteryTicketService {
    private final SecureRandom rng = new SecureRandom();

    private <T> void shuffle(List<T> list) {
        Collections.shuffle(list, rng);
    }
}
```

### 已考慮的替代方案
| 替代方案 | 棄用原因 |
|---|---|
| `ThreadLocalRandom` | 非密碼學安全 |
| `Collections.shuffle()` 預設 | 使用 `Random`；非 CSPRNG |
| 顯式 `/dev/urandom` 串流 | 不必要；Linux 上 `SecureRandom` 已包裝它 |

---

## 3. SCRATCH_PLAYER 開套玩家 Session 鎖定

### 決策
使用**樂觀 DB 鎖定 + 依 `lotteryId` 的單一 `synchronized` JVM 區塊**進行開套 Session 建立，與現有 GACHA 鎖定模式（`ConcurrentHashMap<String, Object> gachaLocks`）相同。單節點 EC2 部署不需要新的分散式鎖基礎設施。

### 理由
- 競態條件為：兩個玩家同時偵測到「無活躍 Session」，兩者都嘗試 INSERT 新 Session 作為開套玩家。
- 預防方式：
  1. `LotteryTicketServiceImpl` 中的 `ConcurrentHashMap<String, Object> sessionLocks` 為每個 `lotteryId` 提供 JVM 層級互斥鎖。
  2. 在鎖內：重新檢查 ACTIVE Session（雙重鎖定檢查）。如果另一個執行緒剛建立了 Session，則使用它。
  3. 若確實沒有 Session：以 `status = ACTIVE` INSERT 新 Session。
- 這相當於資料庫 `INSERT … ON DUPLICATE KEY IGNORE` 模式，但在現有 MyBatis 堆疊中無需更改結構即可運作。

### 開套玩家到期流程

```
Player A draws → no session → acquires sessionLocks[lotteryId]
  → re-check → still no session → INSERT session (openerUserId=A, status=ACTIVE)
  → release lock → start protection window
  
Player B draws (concurrent) → acquires sessionLocks[lotteryId]
  → re-check → session ACTIVE (A is opener) → canDrawNow() → false → 403
  
Protection window expires:
  → canDrawNow() calls LotterySession auto-expire: status→EXPIRED
  → next draw by any player creates new session (they become opener)
```

### 指定閘門（SCRATCH_PLAYER FR-008）

- Session 建立後，`playerDesignatedNumbers` 為 `null`，`status = ACTIVE`。
- 抽獎請求檢查：若 `gameMode == SCRATCH_PLAYER` 且 `session.playerDesignatedNumbers == null` 且呼叫者為開套玩家 → 回傳 `DESIGNATION_REQUIRED` 回應（HTTP 202，`requiresDesignation: true`）。
- 非開套玩家在此狀態下抽獎 → HTTP 423（已鎖定：等待開套玩家指定）。
- 一旦 `POST /designate` 完成 → 設定 `playerDesignatedNumbers` → `autoAssignNonGrandPrizes()` → 抽獎正常進行。

### 已考慮的替代方案
| 替代方案 | 棄用原因 |
|---|---|
| Redis 分散式鎖（Redisson） | 增加基礎設施相依性；單節點 EC2 不需要分散式鎖 |
| 資料庫 Session 列的 `SELECT FOR UPDATE` | 可行但需要在業務邏輯期間保持 DB 交易開啟；有死鎖風險 |
| 悲觀 JPA `@Lock(PESSIMISTIC_WRITE)` | 此 MyBatis 程式庫不使用 |

---

## 4. 資訊隱藏：AVAILABLE 票券回應過濾

### 決策
`LotteryTicketServiceImpl.getTicketsForFrontend()` 已過濾 AVAILABLE 票券。確認並強制執行：response DTO `TicketListResponse` 使用**專用投影**，對任何 `status != DRAWN` 的票券省略 `prizeId`、`prizeLevel`、`prizeName`、`prizeImageUrl`、`revealedNumber`、`isGrandPrize`、`isLastPrize`。

### 理由
SC-001 要求零洩漏。最安全的模式是使用獨立的 DTO 類別（而非實體的子集），如此一來，在實體中新增欄位時，不會意外暴露它。

### DTO 設計

```java
// Safe public view
public record TicketSummary(
    Integer ticketNumber,
    String  status          // "AVAILABLE" | "DRAWN"
    // NO prizeId, NO revealedNumber, NO prizeLevel
) {}

// Full view — only populated for DRAWN tickets
public record TicketDetail(
    Integer         ticketNumber,
    Integer         revealedNumber,   // SCRATCH modes only
    String          prizeId,
    String          prizeLevel,
    String          prizeName,
    String          prizeImageUrl,
    boolean         isGrandPrize,
    String          drawnBy,
    LocalDateTime   drawnAt
) {}
```

---

## 5. 免費抽獎退款 — 邊界案例處理

| 情境 | 行為 |
|---|---|
| 開套玩家在第 1 抽中大獎（在 protectionDraws 內） | 退款 = 1 × pricePerDraw。`freeDrawTriggered=1`。 |
| 開套玩家在第 3 抽中大獎（protectionDraws=5） | 退款 = 3 × pricePerDraw（該時點的 openerTotalCost）。 |
| 開套玩家耗盡 protectionDraws 未中大獎 | 不退款。回合正常繼續。 |
| 非開套玩家在保護視窗內中大獎 | 不退款（FR-011：僅限開套玩家）。 |
| 抽獎活動的 `freeDrawEnabled = 0` | `checkAndTriggerFreeDraw()` 短路 → 不退款。 |
| 分配兩個大獎（例如 A 和 B 等級均為 `isGrandPrize=1`） | 開套玩家抽到的第一個大獎觸發退款；`freeDrawTriggered` 防止雙重退款。 |

---

## 6. 並發控制 — 防止重複抽獎

### 決策
對於 RANDOM 和 SCRATCH 模式：以 `WHERE id = ? AND status = 'AVAILABLE'` 更新票券狀態，並檢查受影響列數 == 1。若受影響 0 列 → 票券已被抽出 → 回傳錯誤。

```sql
UPDATE lottery_ticket
SET status = 'DRAWN', drawn_by = ?, drawn_at = NOW()
WHERE id = ? AND status = 'AVAILABLE'
```

這是**樂觀並發**模式，無需 JVM 鎖。結合 Session 開套玩家鎖，可防止：
- 兩個玩家同時抽取同一張票券。
- 玩家在抽獎活動售罄後繼續抽獎。

### 已考慮的替代方案
| 替代方案 | 棄用原因 |
|---|---|
| `SELECT FOR UPDATE` + UPDATE | 較重；需要 DB 交易隔離 SERIALIZABLE |
| 每張票券的應用程式層級 synchronized | 不可擴展；若橫向擴展多個 JVM 實例則無效 |

---

## 7. 現有實作 — 差距分析

| 需求 | 目前狀態 | 差距 |
|---|---|---|
| FR-001 建立時生成 N 張票券 | `generateTickets()` 存在 | ✅ 已涵蓋 |
| FR-002 RANDOM 安全洗牌 + 獎品預分配 | `generateRandomTickets()` 含洗牌 | ✅ 已涵蓋 |
| FR-003 SCRATCH 獨立 revealed_number 洗牌 | `generateScratchTickets()` 建立 `revealedPool` | ✅ 已涵蓋 |
| FR-004 ticket_number ⊥ revealed_number | 獨立洗牌列表 | ✅ 已涵蓋 |
| FR-005/FR-006 AVAILABLE 資訊隱藏 | `getTicketsForFrontend()` 過濾 | 需要將 GET /tickets 端點接入此方法 |
| FR-007 SCRATCH_STORE 店家在建立時指定 | `designatedPrizeNumbers` 在 generate 中解析 | ✅ 已涵蓋 |
| FR-008 SCRATCH_PLAYER 開套玩家必須先指定 | Session 已建立；抽獎在指定前封鎖 | 閘門邏輯部分實作；需要 202 回應合約 |
| FR-009 自動分配剩餘獎品 | `autoAssignNonGrandPrizes()` 存在 | ✅ 已涵蓋 |
| FR-010 開套玩家 Session 含 protectionDraws + protectionMinutes | `LotterySession` + `canDrawNow()` | ✅ 已涵蓋 |
| FR-011 免費抽獎退款（開套玩家 + 在 protectionDraws 內） | `checkAndTriggerFreeDraw()` | ✅ 已涵蓋 |
| FR-012 並發控制 — 無重複抽獎 | `WHERE status='AVAILABLE'` 樂觀更新 | 確認受影響列數檢查已實作 |
| FR-013 指定端點接受 revealedNumbers | `designatePrizePositions()` 接受 `PrizeDesignation(revealedNumber, prizeId)` | ✅ 已涵蓋 |
| GET /tickets 端點（SC-001） | `getTicketsForFrontend()` Service 方法 | **缺少 Controller 端點** |
| GET /designation-check | 未找到 | **缺少端點** |

**摘要**：兩個新的 Controller 端點是主要交付項目。Service 層大致完整。
