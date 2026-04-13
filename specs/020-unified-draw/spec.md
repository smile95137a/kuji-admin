# 功能規格書：統一抽獎系統

**功能分支**：`020-unified-draw`
**建立日期**：2026-04-13
**狀態**：草稿
**輸入**：統一抽獎入口（Strategy Pattern）、保護時間重構、免單機制、並發控制、扭蛋/一番賞/卡牌/刮刮樂四類合一

## 使用者情境與測試

### 使用者故事 1 — 玩家透過統一入口抽獎（優先級：P1）

身為玩家，我希望不論商品分類如何，都呼叫同一個 API 進行抽獎，由系統自動判斷使用哪種抽獎邏輯。

**此優先級的原因**：目前有 `LotteryDrawController` 和 `RandomDrawController` 兩個分散的入口，增加前端維護成本與邏輯不一致風險。

**獨立測試**：前端呼叫 `POST /api/lottery/{id}/draw`，不論商品是扭蛋、一番賞、卡牌或刮刮樂，都得到正確的抽獎結果。

**驗收情境**：

1. **在** 商品 category=GACHA（扭蛋）的情況下，**當** 呼叫 `POST /api/lottery/{id}/draw { count: 3 }`，**則** 系統走加權隨機邏輯，回傳 3 個獎品結果。
2. **在** 商品 category=OFFICIAL_ICHIBAN（一番賞）的情況下，**當** 呼叫 `POST /api/lottery/{id}/draw { ticketNumber: 5 }`，**則** 系統走籤位制邏輯，回傳第 5 號籤的獎品。
3. **在** 商品 category=TRADING_CARD（卡牌）的情況下，**當** 呼叫 `POST /api/lottery/{id}/draw { ticketNumber: 12 }`，**則** 走籤位制邏輯（與一番賞相同）。
4. **在** 商品 category=CUSTOM_GACHA + gameMode=SCRATCH_STORE（刮刮樂）的情況下，**當** 呼叫 `POST /api/lottery/{id}/draw { ticketNumber: 8 }`，**則** 走刮刮樂邏輯（雙號碼機制）。

---

### 使用者故事 2 — 保護時間自動延長（優先級：P1）

身為玩家，我希望在保護時間內再次操作時，保護時間自動延長（每次 +2 分鐘，最多 10 分鐘），讓我有足夠時間完成抽獎。

**此優先級的原因**：保護時間是排隊機制的核心，必須正確延長以避免被其他玩家搶走。

**驗收情境**：

1. **在** 玩家首次抽獎的情況下，**當** 系統建立保護，**則** 保護時間為 system_config 的 `protection_initial_minutes`（如 5 分鐘）。
2. **在** 玩家在保護內再次操作（無論抽 1 次或 10 次）的情況下，**當** API 呼叫完成，**則** 保護時間延長 `protection_extension_minutes`（如 +2 分鐘），但不超過 `protection_max_minutes`（如 10 分鐘）。
3. **在** 保護時間已達最大值的情況下，**當** 玩家再次操作，**則** 保護時間不再延長。
4. **在** 多抽 10 次的情況下，**當** 一次 API 呼叫，**則** 視為一次操作，延長一次保護時間。

---

### 使用者故事 3 — 並發控制（優先級：P1）

身為系統，我必須確保同一商品不會有兩個玩家同時抽獎，先到先鎖，後到必須 rollback 並通知前端。

**驗收情境**：

1. **在** 玩家 A 正在抽獎（有保護鎖）的情況下，**當** 玩家 B 同時呼叫抽獎 API，**則** B 收到 409 LOTTERY_LOCKED + 剩餘秒數。
2. **在** 兩個請求完全同時到達的情況下，**當** 系統處理，**則** 只有一個成功取得鎖，另一個 rollback 並回傳衝突訊息。
3. **在** 扭蛋（GACHA）的情況下，**當** 多人同時抽獎，**則** 使用 synchronized 或 DB 鎖確保獎品不重複分配。

---

### 使用者故事 4 — 免單機制（優先級：P2）

身為刮刮樂的開套者，我希望在保護時間內的前 N 抽（店家設定）抽中大獎時，系統自動退還我本次開套的全部花費，商品自動下架。

**此優先級的原因**：免單是刮刮樂的核心商業機制，提升開套者的意願。

**驗收情境**：

1. **在** 商品為刮刮樂且 freeDrawThreshold=5 的情況下，**當** 開套者在第 3 抽抽中大獎，**則** 系統退還 3 × pricePerDraw 金幣，商品自動下架。
2. **在** 商品為刮刮樂且 freeDrawThreshold=5 的情況下，**當** 開套者在第 6 抽才抽中大獎，**則** 不觸發免單（超過門檻）。
3. **在** 非開套者（第二位玩家）抽中大獎的情況下，**當** 抽獎完成，**則** 不觸發免單。
4. **在** 免單觸發後，**當** 退款完成，**則** 商品 status → ENDED（自動下架），coin_transaction 有一筆 FREE_DRAW_REFUND 記錄。
5. **在** 保護時間過期後開套者才抽中大獎的情況下，**當** 超時，**則** 視為放棄，不提供免單。

---

### 使用者故事 5 — 多抽驗證（優先級：P1）

身為系統，我必須驗證每次 API 的抽獎次數不超過 system_config 的上限，且不超過商品剩餘庫存。

**驗收情境**：

1. **在** max_draws_per_request=10 的情況下，**當** 玩家傳 count=11，**則** 回傳 400 錯誤。
2. **在** 商品剩餘 5 抽的情況下，**當** 玩家傳 count=10，**則** 回傳 400 錯誤提示庫存不足，或只抽 5 次（依設計選擇）。

---

### 邊界情況

- 抽獎中途伺服器崩潰？@Transactional 確保全部 rollback。
- 玩家帳戶金幣恰好等於 pricePerDraw × count？正常扣款至 0，不報錯。
- 免單後商品已下架，其他玩家的抽獎請求？回傳商品已結束。
- 卡牌與一番賞的前端動畫不同，但後端邏輯完全相同？是，前端自行處理動畫差異。

## 需求規格

### 功能需求

- **FR-001**：統一抽獎入口 `POST /api/lottery/{lotteryId}/draw`，body 支援 `{ count, ticketNumber, tickets }` 三種模式。
- **FR-002**：Strategy Pattern 架構，依 category 派發：
  - GACHA → GachaDrawStrategy（加權隨機）
  - OFFICIAL_ICHIBAN / TRADING_CARD → TicketDrawStrategy（籤位制）
  - CUSTOM_GACHA → ScratchDrawStrategy（刮刮樂雙號碼制）
- **FR-003**：合併 `LotteryDrawController` + `RandomDrawController` → 統一 `DrawController`。
- **FR-004**：保護時間參數從 system_config 讀取（初始/延長/最大）。
- **FR-005**：保護時間延長邏輯：每次 API 呼叫 +extension_minutes，不超過 max_minutes。
- **FR-006**：並發控制：DB 層 SELECT FOR UPDATE + 應用層 synchronized（GACHA）。
- **FR-007**：免單機制：僅刮刮樂 + 開套者 + 前 freeDrawThreshold 抽 + 大獎 → 退款 + 下架。
- **FR-008**：扣款依商品 paymentType 決定（GOLD 或 BONUS），不做混合扣款。
- **FR-009**：多抽上限從 system_config 讀取（max_draws_per_request）。
- **FR-010**：庫存校驗：count > 剩餘庫存 → 拒絕。
- **FR-011**：保留 `POST /api/lottery/{id}/designate`（刮刮樂 SCRATCH_PLAYER 指定大獎位置）。
- **FR-012**：保留 `GET /api/lottery/{id}/session`（取得當前 session 資訊）。

### Strategy Pattern 架構

```java
public interface DrawStrategy {
    DrawResultRes execute(String userId, String lotteryId, DrawRequest request);
}

@Component
public class DrawStrategyFactory {
    public DrawStrategy getStrategy(String category) {
        return switch (LotteryCategoryEnum.fromCode(category)) {
            case GACHA -> gachaDrawStrategy;
            case OFFICIAL_ICHIBAN, TRADING_CARD -> ticketDrawStrategy;
            case CUSTOM_GACHA -> scratchDrawStrategy;
        };
    }
}
```

### 統一 DrawRequest

```java
@Data
public class DrawRequest {
    @Min(1) 
    private Integer count = 1;       // 扭蛋：抽幾次
    private Integer ticketNumber;    // 一番賞/卡牌/刮刮樂：選中的號碼
    private List<String> tickets;    // 多張籤位 UUID（批量）
}
```

### 統一 DrawResultRes

```java
@Data @Builder
public class DrawResultRes {
    private List<DrawRecord> draws;       // 抽獎結果
    private Integer lotteryRemaining;     // 剩餘抽數
    private Boolean lockAcquired;         // 是否取得保護鎖
    private String lockExpiresAt;         // 保護到期時間
    private Boolean priceMayHaveChanged;  // 是否觸發降價
    private Long newPricePerDraw;         // 新價格
    private Boolean freeDrawTriggered;    // 是否觸發免單
    private Long freeDrawRefundAmount;    // 免單退款金額
    private Boolean designationRequired;  // 是否需要指定大獎位置
}
```

### 核心實體

不新增實體。使用現有的 Lottery、LotteryPrize、LotteryTicket、LotterySession、LotteryDrawRecord。

## 成功標準

- **SC-001**：前端只需呼叫 1 個 draw API，不需判斷分類。
- **SC-002**：保護時間延長邏輯正確（初始→延長→最大值封頂）。
- **SC-003**：免單僅在刮刮樂 + 開套者 + 門檻內 + 大獎時觸發。
- **SC-004**：零超額抽獎（抽獎次數不超過庫存）。
- **SC-005**：`RandomDrawController` 刪除，功能整合至統一 `DrawController`。
- **SC-006**：`mvn clean package -DskipTests` 編譯通過。

## 假設前提

- 前端統一呼叫 `POST /api/lottery/{id}/draw`，body 格式依使用情境不同而異，前端根據商品 category 決定傳哪些欄位。
- 卡牌與一番賞後端邏輯 100% 相同，差異僅在前端動畫。
- 免單退款的幣種與商品 paymentType 一致。
- 保護時間對所有分類生效（包括扭蛋），確保排隊公平性。
