# 實作計畫：刮刮樂獎項制度修正

**分支**：`015-scratch-lottery-prize-rules` | **規格**：`specs/015-scratch-lottery-prize-rules/spec.md`

---

## 摘要

修正刮刮樂（SCRATCH_CARD）系統中三個核心問題：

1. **獎品規則未強制執行**：刮刮樂模式下必須只有 1 個大獎（數量=1），其餘全為謝謝惠顧，但目前後台新增獎品時沒有任何驗證。
2. **玩家指定大獎的畫面沒出現**：`checkDesignationRequired()` 只在 `isOpener=true` 時執行，非開套玩家可以繞過直接抽獎；且 `gameMode` 欄位若未設定，整個判斷都會靜默跳過。
3. **SCRATCH_STORE 缺少上架前置條件**：`updateStatus()` 和 `changeStatus()` 沒有在「店家模式」上架前確認 `designatedPrizeNumbers` 是否已設定。

**補充（需求釐清後的規則）**：
- SCRATCH_PLAYER 模式：開套玩家有 **10 分鐘**指定大獎，逾時自動釋放，下一位玩家成為新開套者
- 等待期間，非開套玩家看到倒數計時，不得抽獎
- SCRATCH_PLAYER 可直接上架，玩家進場後在遊戲內指定大獎
- SCRATCH_STORE 必須先指定大獎號碼，才能上架

---

## 技術環境

| 項目 | 內容 |
|------|------|
| 語言 | Java 21 |
| 框架 | Spring Boot 3.3.3 + MyBatis 3.0.5 |
| 資料庫 | MySQL 8.x |
| 建構 | `mvn clean package -DskipTests` |
| 重新產生 Mapper | `mvn mybatis-generator:generate` |

---

## 根本原因分析

### Bug A — 非開套玩家沒有被攔截
**位置**：`LotteryDrawController.draw()`

```java
if (session.isOpener()) {          // ← 只有開套者才被檢查
    DesignationRequiredResponse check = checkDesignationRequired(lotteryId, session);
    ...
}
// 非開套玩家直接往下執行抽獎，完全沒有被攔截
```

**影響**：SCRATCH_PLAYER 模式下，開套者還沒指定大獎時，其他玩家可以直接抽獎。

---

### Bug B — `gameMode` 欄位可能為 null
**位置**：`checkDesignationRequired()` 中的判斷

```java
if (!"SCRATCH_PLAYER".equals(lottery.getGameMode())) {
    return null;  // gameMode 為 null 時也會 return null，靜默跳過
}
```

**影響**：如果後台新增商品時沒有正確儲存 `gameMode` 欄位，指定大獎的整個流程都不會觸發。

---

### Bug C — `LotterySession` 缺少 `designationDeadline` 欄位
**位置**：`lottery_session` 資料表 / `LotterySession` 實體類

目前沒有任何機制讓開套者的「指定期限」逾時，若開套者永遠不指定，後續玩家會無限期等待。

---

### Bug D — SCRATCH_STORE 缺少上架前置驗證
**位置**：`LotteryServiceImpl.updateStatus()` / `changeStatus()`

沒有在 `ON_SHELF` 前檢查 `designatedPrizeNumbers` 是否已設定，
導致店家可以在未指定大獎號碼的情況下直接上架刮刮樂。

---

### Bug E — 刮刮樂模式沒有獎品數量驗證
**位置**：`LotteryServiceImpl.createLotteryWithPrizes()`（及更新流程）

沒有任何限制阻止後台設定多個獎品或非大獎獎品，違反「1 大獎 + 其餘謝謝惠顧」的規則。

---

## 受影響的檔案

| 檔案 | 變更內容 |
|------|---------|
| `sql/add-designation-deadline.sql` | **新建**：DB 遷移腳本 |
| `entity/LotterySession.java` | MBG 重新產生（加入 `designationDeadline`）|
| `example/LotterySessionExample.java` | MBG 重新產生 |
| `mapper/LotterySessionMapper.java` | MBG 重新產生 |
| `resources/mapper/LotterySessionMapper.xml` | MBG 重新產生 |
| `service/LotteryTicketService.java` | 擴充 `SessionInfo` record |
| `service/impl/LotteryTicketServiceImpl.java` | Session 逾時邏輯 |
| `service/impl/LotteryServiceImpl.java` | 上架前置驗證 + 獎品數量驗證 |
| `controller/api/LotteryDrawController.java` | 非開套者攔截 + 倒數計時回應 |

---

## 階段一：DB 遷移

**新建檔案**：`sql/add-designation-deadline.sql`

```sql
-- 新增指定大獎截止時間欄位（SCRATCH_PLAYER 模式，開套者有 10 分鐘）
ALTER TABLE lottery_session
  ADD COLUMN designation_deadline DATETIME NULL
  COMMENT '指定大獎截止時間（SCRATCH_PLAYER 模式，10分鐘）';
```

執行後重新產生 Mapper：
```bash
mvn mybatis-generator:generate
```

---

## 階段二：擴充 SessionInfo Record

**位置**：`service/LotteryTicketService.java`

在 `SessionInfo` record 尾端新增 `designationDeadline`：

```java
record SessionInfo(
    String sessionId,
    String lotteryId,
    String openerUserId,
    boolean isOpener,
    int protectionDraws,
    LocalDateTime protectionEndTime,
    int openerDrawCount,
    boolean freeDrawEnabled,
    String status,
    String playerDesignatedNumbers,
    LocalDateTime designationDeadline   // ← 新增：開套者指定大獎的截止時間
) {}
```

同步更新 `LotteryTicketServiceImpl` 中兩處 `new SessionInfo(...)` 的建構呼叫。

---

## 階段三：getOrCreateSession — 10 分鐘逾時邏輯

**位置**：`LotteryTicketServiceImpl.getOrCreateSession()`

**情況 A：查到現有 ACTIVE 場次 + SCRATCH_PLAYER 模式 + 尚未指定 + 已超過截止時間**
→ 將場次標記為 EXPIRED，以當前使用者為開套者建立新場次（`designationDeadline = now + 10 分鐘`）

**情況 B：建立新場次**
```java
// 若為 SCRATCH_PLAYER 模式，設定 10 分鐘指定截止時間
if ("SCRATCH_PLAYER".equals(lottery.getGameMode())) {
    newSession.setDesignationDeadline(LocalDateTime.now().plusMinutes(10));
}
```

---

## 階段四：攔截非開套玩家（抽獎 Controller）

**位置**：`LotteryDrawController.draw()`

在現有開套者判斷之後，新增非開套者的攔截邏輯：

```java
// 攔截非開套玩家（等待開套者指定大獎中）
if (!session.isOpener() && "SCRATCH_PLAYER".equals(lottery.getGameMode())) {
    boolean notDesignated = session.playerDesignatedNumbers() == null
            || session.playerDesignatedNumbers().isBlank();
    if (notDesignated) {
        String deadline = session.designationDeadline() != null
                ? session.designationDeadline().toString() : null;
        return ResponseEntity.ok(new DesignationPendingResponse(
                true,
                "開套玩家正在指定大獎位置，請稍候",
                deadline));  // 前端用此時間顯示倒數計時
    }
}
```

新增回應 DTO：
```java
/**
 * 等待開套者指定大獎時的回應（非開套玩家用）
 */
public record DesignationPendingResponse(
    boolean awaitingDesignation,  // 固定 true
    String message,
    String openerDeadline         // ISO-8601，前端用來倒數計時
) {}
```

---

## 階段五：SCRATCH_STORE 上架前置驗證

**位置**：`LotteryServiceImpl`

新增驗證輔助方法，並在 `updateStatus()` 和 `changeStatus()` 中，
狀態要變為 `ON_SHELF` 之前呼叫：

```java
private void validateCanGoOnShelf(Lottery lottery) {
    if ("SCRATCH_STORE".equals(lottery.getGameMode())) {
        String d = lottery.getDesignatedPrizeNumbers();
        if (d == null || d.isBlank()) {
            throw new BusinessException(
                "SCRATCH_STORE 模式：請先在後台指定大獎號碼（designatedPrizeNumbers），才能上架");
        }
    }
}
```

呼叫位置：
- `updateStatus()` 中 `"ON_SHELF".equals(status)` 的判斷之前
- `changeStatus()` 中 `"ON_SHELF".equals(targetStatus)` 的判斷之前

---

## 階段六：刮刮樂獎品數量驗證

**位置**：`LotteryServiceImpl`

新增驗證方法，在新增與更新獎品的流程中呼叫：

```java
private void validateScratchPrizes(String gameMode, List<?> prizes) {
    if (!"SCRATCH_STORE".equals(gameMode) && !"SCRATCH_PLAYER".equals(gameMode)) return;

    long grandPrizeTotal = prizes.stream()
        .filter(p -> Boolean.TRUE.equals(p.getIsGrandPrize()))
        .mapToLong(p -> p.getQuantity() != null ? p.getQuantity() : 1)
        .sum();

    if (grandPrizeTotal != 1) {
        throw new BusinessException(
            "刮刮樂模式：大獎數量必須剛好為 1（目前設定 " + grandPrizeTotal + " 個）");
    }

    long nonGrandCount = prizes.stream()
        .filter(p -> !Boolean.TRUE.equals(p.getIsGrandPrize()))
        .count();

    if (nonGrandCount > 0) {
        throw new BusinessException(
            "刮刮樂模式：不允許設定非大獎獎品（其餘籤位自動為謝謝惠顧，無需設定）");
    }
}
```

---

## 階段七：建構驗證

```bash
mvn clean package -DskipTests
```

預期結果：`BUILD SUCCESS`，0 個錯誤。

---

## 待辦清單

| ID | 標題 | 階段 |
|----|------|------|
| `db-migration` | 新增 designation_deadline 欄位 + 執行 MBG | 1 |
| `session-info-record` | 擴充 SessionInfo record | 2 |
| `session-timeout` | getOrCreateSession：10 分鐘逾時邏輯 | 3 |
| `draw-nonopener-block` | 攔截等待指定的非開套玩家 | 4 |
| `onshelf-gate` | SCRATCH_STORE 上架前置驗證 | 5 |
| `prize-validation` | 刮刮樂強制 1 大獎規則 | 6 |
| `build-verify` | mvn clean package -DskipTests | 7 |
