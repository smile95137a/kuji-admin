# 研究報告： 獎品盒 (Prize Box) — Feature 010

**階段**：0 — 大綱與研究  
**日期**：2026-03-22  
**分支**：`010-prize-box`

---

## R-001: 現有實作盤點

### 決策
本功能已有初步骨架，主體流程可用，但需針對規格差距進行補強。

### 現有實作
| 元件 | 狀態 | 路徑 |
|------|------|------|
| `PrizeBox` entity | ✅ 存在 | `entity/PrizeBox.java` |
| `PrizeBoxController` | ✅ 存在，缺 `/history` | `controller/api/PrizeBoxController.java` |
| `PrizeBoxService` / `Impl` | ✅ 存在，邏輯有 bug | `service/PrizeBoxService.java` |
| `PrizeBoxMapper` + `Example` | ✅ 存在 | `mapper/PrizeBoxMapper.java` |
| `PrizeBoxStatusEnum` | ✅ 存在，術語待修 | `enums/PrizeBoxStatusEnum.java` |
| `PrizeBoxShipReq` | ✅ 存在，缺 import + userAddressId | `req/prizebox/PrizeBoxShipReq.java` |
| `PrizeBoxRecycleReq` | ✅ 存在 | `req/prizebox/PrizeBoxRecycleReq.java` |
| `PrizeBoxItemRes` | ✅ 存在，缺 isShippable/prizeValue | `res/prizebox/PrizeBoxItemRes.java` |
| `PrizeBoxSummaryRes` | ✅ 存在 | `res/prizebox/PrizeBoxSummaryRes.java` |
| Service tests | ❌ 缺失 | — |

### 理由
直接修補現有程式碼，不重新設計架構，最小化破壞性變更。

---

## R-002: 術語正名 — 賞品盒 vs 獎品盒

### 決策
**使用 `獎品盒`（canonical）**，廢棄 `賞品盒`（deprecated）。

### 變更範圍
- **DB column/table name**：不變（`prize_box`，中性英文名稱）  
- **API path**：不變（`/prize-box`，已上線）  
- **Java enum code**：不變（`IN_BOX`, `SHIPPED`, `RECYCLED`）  
- **需替換**：
  - `PrizeBoxStatusEnum` 的 `name` 欄位（`"在賞品盒中"` → `"在獎品盒中"`）
  - 所有 JavaDoc / 日誌字串中的「賞品盒」
  - Controller/Service 類別 Javadoc 標頭

### 理由
術語一致性是前台產品文案的基礎，但 API path 和 DB schema 已穩定，不值得 breaking change。

### 替代方案考量
- 全面重命名（DB+API）→ 破壞已上線 API，成本過高，拒絕。

---

## R-003: `isRecyclable` 邏輯修正

### 決策
`isRecyclable = (prizeBox.getRecycleBonus() != null && prizeBox.getRecycleBonus() > 0)`

### 問題
現行 `PrizeBoxServiceImpl.convertToItemRes()` 硬編碼：
```java
.isRecyclable(true) // 在賞品盒中的都可以回收
```
這違反規格：回收 Bonus 為 0 表示不可回收（Clarification 2026-03-22）。

### 解決方案
```java
.isRecyclable(prizeBox.getRecycleBonus() != null && prizeBox.getRecycleBonus() > 0)
```

同步在 `recyclePrizes` 方法加入驗證，若 `recycleBonus <= 0` 拋出 `BusinessException("此獎品不可回收")`。

### 理由
規格 FR-007 + Clarification 明確：店家設定 `recycleBonus = 0` 代表不可回收。

---

## R-004: `isShippable` 欄位

### 決策
`PrizeBox` entity 中存在 `isShippable`（Byte）欄位（待確認），或由業務規則決定：若來源一番賞（Lottery）仍處於 ACTIVE/ENDED 狀態且店家未停用，則可出貨。

### 調查
- 現行 `PrizeBox` entity 欄位清單不含明確 `isShippable`，但規格 FR-011 要求標示。
- 現行 `shipPrizes` 未驗證可出貨旗標（規格 US2 AC3 要求「不可出貨的獎品被拒絕選取」）。

### 決策
在 `PrizeBox` entity 中確認或新增 `isShippable` (TINYINT DEFAULT 1)；  
`PrizeBoxItemRes` 新增 `isShippable` 欄位；  
`shipPrizes` 加入驗證：`prizeBox.getIsShippable() == 0` → 拒絕並拋出 `BusinessException`。

### 理由
FR-011 明確要求；邊界情況 US2 AC3 直接依賴此欄位。

---

## R-005: 歷史端點 `GET /prize-box/history`

### 決策
新增端點：`GET /api/prize-box/history`，回傳玩家所有獎品（含 SHIPPED、RECYCLED），不含狀態篩選，按 `created_at DESC` 排序。

### 問題
現有 `getPrizeBox` 僅回傳 `IN_BOX` 狀態。規格 US4 及前台歷史頁面需要完整記錄。

### 解決方案
```java
// PrizeBoxService 新增
List<PrizeBoxItemRes> getPrizeBoxHistory(String userId);
```
```sql
-- 無 status filter，全部查詢
WHERE user_id = #{userId} ORDER BY created_at DESC
```

### 理由
歷史記錄是常見需求，無任何技術障礙，僅需補充端點。

---

## R-006: `PrizeBoxShipReq` 缺少 `@NotBlank` import

### 決策
補上 `import jakarta.validation.constraints.NotBlank;`

### 問題
`PrizeBoxShipReq.java` 使用 `@NotBlank` annotation 但缺少對應 import，導致編譯錯誤。

### 理由
編譯阻斷問題。需立即修正。

---

## R-007: UserAddress 整合

### 決策
`PrizeBoxShipReq` 新增選填欄位 `userAddressId`（String）。  
`PrizeBoxServiceImpl.shipPrizes` 處理優先順序：
1. 若提供 `userAddressId` → 從 `UserAddress` 查詢填入 `recipientName`, `recipientPhone`, `recipientAddress`
2. 否則使用請求中的 `recipientName/Phone/Address`
3. 否則從 `User` 個人資料帶入（現有邏輯）

### 理由
規格 US4 (P3)：玩家建立出貨時能使用先前儲存的收件地址。`UserAddress` 實體已存在（`entity/UserAddress.java`）。

### 替代方案考量
- 前台自行拆分地址欄位傳入 → 可行，但不如 `userAddressId` 方便，且有重複邏輯。

---

## R-008: 原子性與並發保護

### 決策
使用 `@Transactional` + 資料庫層面的行鎖（SELECT FOR UPDATE 或樂觀鎖）確保同一獎品不被重複出貨/回收。

### 問題
邊界情況：兩台設備同時嘗試出貨相同獎品（競態條件）。

### 解決方案
在 `shipPrizes` 和 `recyclePrizes` 中，驗證獎品狀態後立即更新（在同一事務內）；若 `status != IN_BOX` 則拋出例外。Spring 的 `@Transactional` + InnoDB 行鎖在大多數情況可保護。若需更強保護，可在 mapper 加入 `SELECT ... FOR UPDATE`。

### 理由
現行架構已使用 `@Transactional`，InnoDB 行鎖足以應對低並發場景。

---

## R-009: prizeValue 欄位

### 決策
`PrizeBoxItemRes` 新增 `prizeValue`（Long），從 `LotteryPrize.prizeValue`（或類似欄位）填入。

### 問題
規格 FR-002 要求顯示「獎品價值」，現行 `PrizeBoxItemRes` 缺此欄位。

### 待調查事項
確認 `LotteryPrize` entity 是否有 `priceValue` 或 `prize_value` 欄位。若無，`prizeValue` 可先設為 0 或 null，保留欄位供未來填充。

---

## R-010: 端點路徑前綴

### 決策
保持現有 `@RequestMapping("/prize-box")`，Spring Security 已配置 `/api/**` 前綴由 JWT filter 保護。

### 理由
現行架構前台 API 統一在 `/api/prize-box/**`（由 context path 或 Security config 添加 `/api` 前綴），無需修改 Controller 路徑。

---

## 差距摘要

| # | 問題 | 優先級 | 類型 |
|---|------|--------|------|
| 1 | `isRecyclable` 硬編碼 `true` | P1 | 錯誤修正 |
| 2 | `@NotBlank` import 缺失 | P1 | 錯誤修正（編譯）|
| 3 | 術語「賞品盒」→「獎品盒」 | P1 | 術語 |
| 4 | `GET /prize-box/history` 缺失 | P1 | 功能缺口 |
| 5 | `isShippable` 欄位/驗證缺失 | P1 | 功能缺口 |
| 6 | `prizeValue` 欄位缺失 | P2 | 功能缺口 |
| 7 | `userAddressId` 整合 | P3 | 功能缺口 |
| 8 | 缺乏單元測試 | P2 | 品質 |
