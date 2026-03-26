# 快速入門：抽獎票券系統（雙號碼與刮刮樂機制）

**功能**: 005-lottery-ticket-system  
**分支**: `005-lottery-ticket-system`  
**技術棧**: Spring Boot 3.3.3 · Java 21 · MyBatis 3.0.5 · MySQL 8.3

---

## 本功能新增項目

本功能完成 `com.group.admin` 中的**雙號碼抽獎票券系統**。Service 層（`LotteryTicketServiceImpl`）大致已實作完成。主要交付項目為：

| 交付項目 | 類型 | 狀態 |
|---|---|---|
| `GET /api/lottery/{id}/tickets` | 新 Controller 端點 | 🆕 新增 |
| `GET /api/lottery/{id}/designation-check` | 新 Controller 端點 | 🆕 新增 |
| `TicketListResponse` DTO | 新 DTO 類別 | 🆕 新增 |
| `DesignationCheckResponse` DTO | 新 DTO 類別 | 🆕 新增 |
| SCRATCH_PLAYER 抽獎閘門（202） | 強化現有 `POST /draw` | 🔧 修改 |
| 樂觀鎖受影響列數檢查 | 強化 `LotteryTicketServiceImpl.draw()` | 🔧 驗證/修正 |
| `LotteryTicketServiceTest` | 單元測試 | 🆕 新增 |

---

## 前置條件

### 1. 驗證資料庫結構

```sql
-- Run against dev/prod MySQL
SHOW COLUMNS FROM lottery_ticket;
SHOW COLUMNS FROM lottery_session;

-- Should include all columns in data-model.md
-- Expected: ticket_number, revealed_number, is_designated_prize, designated_by
-- If missing, apply migration (see data-model.md § Database Migration Notes)
```

### 2. 驗證索引

```sql
SHOW INDEX FROM lottery_ticket;
-- Check: idx_lottery_status (lottery_id, status)
-- Check: idx_lottery_revealed (lottery_id, revealed_number)

-- Add if missing:
ALTER TABLE lottery_ticket
  ADD INDEX idx_lottery_status   (lottery_id, status),
  ADD INDEX idx_lottery_revealed (lottery_id, revealed_number);
```

### 3. 本地建構與執行

```bash
cd C:\Users\user\OneDrive\Desktop\dream\KUJI-Server\admin

# Start with dev profile (application-dev.yml)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

伺服器在連接埠 `8080` 啟動（或依 `application-dev.yml` 中的設定）。

---

## 實作步驟（依序）

### 步驟 1 — 新增 Response DTO

**檔案**: `src/main/java/com/group/admin/res/lottery/TicketListResponse.java`

```java
@Data
public class TicketListResponse {
    private String lotteryId;
    private String gameMode;
    private int totalTickets;
    private int availableCount;
    private int drawnCount;
    private List<TicketView> tickets;

    @Data
    public static class TicketView {
        private Integer ticketNumber;
        private String  status;
        // DRAWN-only fields (null for AVAILABLE)
        private Integer revealedNumber;   // SCRATCH only
        private String  prizeId;
        private String  prizeLevel;
        private String  prizeName;
        private String  prizeImageUrl;
        private Boolean isGrandPrize;
        private String  drawnBy;
        private String  drawnAt;
    }
}
```

**檔案**: `src/main/java/com/group/admin/res/lottery/DesignationCheckResponse.java`

```java
@Data
public class DesignationCheckResponse {
    private boolean required;
    private String  gameMode;
    private String  sessionId;
    private boolean isOpener;
    // Opener fields (when required=true && isOpener=true)
    private Integer         requiredDesignationCount;
    private List<GrandPrize> grandPrizes;
    private List<Integer>   availableRevealedNumbers;
    // Non-opener fields (when required=true && isOpener=false)
    private String openerNickname;
    private String message;
    // Post-designation
    private Boolean alreadyDesignated;

    @Data
    public static class GrandPrize {
        private String prizeId;
        private String prizeName;
        private String prizeLevel;
        private String prizeImageUrl;
    }
}
```

---

### 步驟 2 — 新增 Service 方法

在 `LotteryTicketService.java`（介面）中新增：

```java
/**
 * Returns filtered ticket list for frontend (FR-005, FR-006).
 * AVAILABLE tickets expose only ticketNumber and status.
 */
TicketListResponse getTicketsForFrontend(String lotteryId);

/**
 * Returns designation status for SCRATCH_PLAYER lotteries.
 * Safe to call for any gameMode — returns required=false for non-SCRATCH_PLAYER.
 */
DesignationCheckResponse getDesignationStatus(String lotteryId, String userId);
```

在 `LotteryTicketServiceImpl.java` 中實作兩個方法，接入現有的內部方法。

---

### 步驟 3 — 新增 Controller 端點

在 `LotteryDrawController.java` 中新增：

```java
// GET /api/lottery/draw/{lotteryId}/tickets
@GetMapping("/{lotteryId}/tickets")
@Operation(summary = "List lottery tickets (info-hiding enforced)")
public ResponseEntity<TicketListResponse> getTickets(
        @PathVariable String lotteryId) {
    return ResponseEntity.ok(ticketService.getTicketsForFrontend(lotteryId));
}

// GET /api/lottery/draw/{lotteryId}/designation-check
@GetMapping("/{lotteryId}/designation-check")
@Operation(summary = "Check if SCRATCH_PLAYER designation is required")
public ResponseEntity<DesignationCheckResponse> designationCheck(
        @PathVariable String lotteryId,
        @AuthenticationPrincipal UserDetails userDetails) {
    String userId = extractUserId(userDetails);
    return ResponseEntity.ok(ticketService.getDesignationStatus(lotteryId, userId));
}
```

---

### 步驟 4 — 強化 POST /draw 的 SCRATCH_PLAYER 閘門

在 `LotteryTicketServiceImpl.draw()` 中，取得/建立 Session 後：

```java
// SCRATCH_PLAYER designation gate (FR-008)
if ("SCRATCH_PLAYER".equals(lottery.getGameMode())) {
    LotterySession session = getOrCreateSession(lotteryId, userId);
    if (session.getPlayerDesignatedNumbers() == null) {
        if (userId.equals(session.getOpenerUserId())) {
            // Return 202 with requiresDesignation=true
            return DrawResult.designationRequired(session.getId(), grandPrizeCount, availableRevealedNums);
        } else {
            throw new LotteryException(LotteryErrorCode.DESIGNATION_PENDING);
        }
    }
}
```

---

### 步驟 5 — 驗證 draw() 中的樂觀鎖

確認 `LotteryTicketServiceImpl.draw()` 檢查受影響列數：

```java
int updated = ticketMapper.updateStatusToDrawn(ticket.getId(), userId);
if (updated == 0) {
    throw new LotteryException(LotteryErrorCode.TICKET_ALREADY_DRAWN);
}
```

Mapper SQL 必須使用：
```xml
<update id="updateStatusToDrawn">
  UPDATE lottery_ticket
  SET status='DRAWN', drawn_by=#{userId}, drawn_at=NOW()
  WHERE id=#{id} AND status='AVAILABLE'
</update>
```

---

## 測試

### 執行所有單元測試

```bash
mvn test -pl . -Dtest=LotteryTicketServiceTest
```

### 手動冒煙測試（Postman / curl）

**1. 建立含 5 張票券的 RANDOM 模式抽獎活動**（管理員 API），然後：

```bash
# List tickets (should show only ticketNumber + status)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/lottery/draw/{lotteryId}/tickets

# Draw ticket #3
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"ticketNumber":3}' \
  http://localhost:8080/api/lottery/draw/{lotteryId}/draw

# List tickets again — ticket #3 should now show prize info
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/lottery/draw/{lotteryId}/tickets
```

**2. 測試 SCRATCH_PLAYER 指定流程：**

```bash
# Step 1: Draw as Player A (becomes opener) → expect 202 requiresDesignation=true
# Step 2: Check designation status
curl -H "Authorization: Bearer $TOKEN_A" \
  http://localhost:8080/api/lottery/draw/{lotteryId}/designation-check

# Step 3: Designate as opener
curl -X POST -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{"designations":[{"revealedNumber":23,"prizeId":"grand-uuid"}]}' \
  http://localhost:8080/api/lottery/draw/{lotteryId}/designate

# Step 4: Now draw proceeds normally
curl -X POST -H "Authorization: Bearer $TOKEN_A" \
  -H "Content-Type: application/json" \
  -d '{"ticketNumber":3}' \
  http://localhost:8080/api/lottery/draw/{lotteryId}/draw
```

**3. 測試資訊隱藏（SC-001）：**

建立抽獎活動後，呼叫 `GET /tickets` 並驗證 AVAILABLE 票券的回應 JSON **不包含** `prizeId`、`prizeLevel`、`prizeName`、`prizeImageUrl`、`revealedNumber` 或 `isGrandPrize` 欄位。

---

## 關鍵檔案參考

| 檔案 | 用途 |
|---|---|
| `src/main/java/com/group/admin/controller/api/LotteryDrawController.java` | REST 端點 |
| `src/main/java/com/group/admin/service/LotteryTicketService.java` | Service 介面 |
| `src/main/java/com/group/admin/service/impl/LotteryTicketServiceImpl.java` | 核心業務邏輯 |
| `src/main/java/com/group/admin/entity/LotteryTicket.java` | 票券實體 |
| `src/main/java/com/group/admin/entity/LotterySession.java` | Session 實體 |
| `src/main/resources/mapper/LotteryTicketMapper.xml` | MyBatis SQL |
| `src/main/resources/mapper/LotterySessionMapper.xml` | MyBatis SQL |
| `src/test/java/com/group/admin/service/LotteryTicketServiceTest.java` | 單元測試 |

---

## 遊戲模式快速參考

| `gameMode` | 票券生成 | revealed_number | 大獎分配 | 免費抽獎 |
|---|---|---|---|---|
| `RANDOM` | 洗牌獎品→格子 | NULL | 在 `generateTickets()` 時 | ✅ 若啟用 |
| `SCRATCH_STORE` | 兩個獨立洗牌 | 1-N，隱藏 | 店家透過建立時的 `designatedPrizeNumbers` | ✅ 若啟用 |
| `SCRATCH_PLAYER` | 兩個獨立洗牌 | 1-N，隱藏 | 開套玩家在第一次抽獎後透過 `POST /designate` | ✅ 若啟用 |

---

## 故障排除

| 症狀 | 可能原因 | 修正方式 |
|---|---|---|
| `GET /tickets` 回傳 404 | 端點尚未新增至 Controller | 依步驟 3 新增 `@GetMapping("/{lotteryId}/tickets")` |
| AVAILABLE 票券回應包含 `prizeId` | `getTicketsForFrontend()` 未接入新端點 | 確認 Controller 呼叫 `ticketService.getTicketsForFrontend()`，而非原始 Mapper |
| `POST /draw` 在 SCRATCH_PLAYER 時回傳 500 | 指定閘門未實作 | 套用步驟 4 |
| 兩個使用者都成為開套玩家 | `sessionLocks` 未使用 | 將 `getOrCreateSession()` 包裝在 `synchronized(sessionLocks.computeIfAbsent(...))` 中 |
| 免費抽獎觸發兩次 | 缺少 `freeDrawTriggered` 檢查 | 驗證 `checkAndTriggerFreeDraw()` 在退款前檢查 `session.freeDrawTriggered == 0` |
