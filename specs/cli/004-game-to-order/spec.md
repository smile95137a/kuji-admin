# 前端規格書（前台）：遊戲至訂單流程

**功能分支**：`cli/004-game-to-order`
**對應後端 Spec**：`specs/004-game-to-order/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：需登入（ROLE_USER）

---

## 頁面與介面清單

### 頁面 1 — 抽獎結果後動作 Modal
- **路由**：`/lottery/{id}/draw`（結果 overlay）
- **存取權限**：需登入
- **UI 元件**：
  - 賞品揭示卡片（Prize Reveal Card）
  - 「繼續抽」按鈕（CTA）
  - 「前往賞品盒」按鈕（CTA）
  - 「查看本次結果」按鈕（CTA）
  - 本次累積賞品摘要（如多次連抽後）

### 頁面 2 — 賞品盒頁
- **路由**：`/prize-box`
- **存取權限**：需登入
- **UI 元件**：
  - 賞品列表（依店家分組）
  - 多選 Checkbox（每個賞品卡片）
  - 全選按鈕（同店家內）
  - 「申請出貨」行動按鈕（至少選一項可出貨賞品）
  - 「回收換分」行動按鈕（至少選一項可回收賞品）
  - 狀態篩選 Tabs（全部 / 在賞品盒 / 已出貨 / 已回收）
  - 賞品狀態標籤（IN_BOX / SHIPPED / RECYCLED）

### 頁面 3 — 回收確認 Modal
- **路由**：`/prize-box`（overlay）
- **UI 元件**：
  - 待回收賞品列表
  - 預計獲得 Bonus 合計
  - 不可逆警告文字
  - 確認回收按鈕
  - 取消按鈕

### 頁面 4 — 訂單列表頁
- **路由**：`/orders`
- **存取權限**：需登入
- **UI 元件**：
  - 訂單卡片列表（orderNo, storeName, status badge, createdAt）
  - 狀態篩選器（全部 / 待出貨 / 準備中 / 已出貨 / 已完成）
  - 空狀態插圖（尚無訂單）
  - 點擊卡片導向訂單詳情

---

## 使用者情境與測試

### 使用者故事 1 — 抽獎後選擇前往賞品盒（優先級：P1）

玩家完成抽獎後，從結果 Modal 點擊「前往賞品盒」，導向賞品盒並看到新獲得的賞品。

**此優先級的原因**：抽獎→賞品盒→出貨是平台核心用戶旅程，每個步驟必須流暢銜接。

**獨立測試**：
1. 完成一次抽獎。
2. 在結果 Modal 點擊「前往賞品盒」。
3. 確認 `/prize-box` 頁面頂部顯示剛獲得的賞品。

**驗收情境**：
1. **在** 抽獎結果 Modal 顯示，**當** 玩家點擊「前往賞品盒」，**則** 導向 `/prize-box`，並高亮顯示（如閃爍動畫）本次新獲得的賞品。
2. **在** 賞品盒頁載入，**當** 呼叫 `GET /api/prize-box`，**則** 最新獲得的賞品（按 `receivedAt` 降序）排在頂部。
3. **在** 抽獎結果 Modal，**當** 玩家點擊「繼續抽」，**則** 關閉 Modal，返回票券格子，不導向賞品盒。

### 使用者故事 2 — 選取賞品申請出貨（優先級：P1）

玩家在賞品盒選取一或多個 `IN_BOX` 賞品，點擊「申請出貨」進入出貨流程。

**此優先級的原因**：這是玩家獲取實體商品的唯一途徑。

**驗收情境**：
1. **在** 賞品盒顯示賞品列表，**當** 玩家勾選 2 個同店家的 `IN_BOX` 賞品，**則** 「申請出貨」按鈕啟用，顯示「申請出貨 (2)」。
2. **在** 玩家點擊「申請出貨」，**當** 按鈕已啟用，**則** 將選中的 `prizeBoxIds` 帶入出貨表單（導向 `/prize-box/ship` 或開啟出貨 Modal）。
3. **在** 玩家選取來自 2 個不同店家的賞品，**當** 點擊「申請出貨」，**則** 顯示警告「將拆分為 2 筆訂單」，需玩家確認後繼續。
4. **在** 賞品 `isShippable = false`，**當** 玩家嘗試勾選，**則** Checkbox 禁用，Tooltip 顯示「此賞品無法出貨」。

### 使用者故事 3 — 回收賞品換 Bonus（優先級：P2）

玩家選取可回收賞品，確認後換取 Bonus 點數。

**此優先級的原因**：回收換分是保留玩家的留存機制，謝謝惠顧賞品的唯一正向出口。

**驗收情境**：
1. **在** 玩家勾選 3 個 `isRecyclable = true` 賞品，**當** 點擊「回收換分」，**則** 顯示確認 Modal，Modal 中列出賞品名稱及「預計獲得 Bonus：XX 點」。
2. **在** 確認 Modal 顯示，**當** 玩家點擊「確認回收」，**則** 呼叫 `POST /api/prize-box/recycle`，按鈕禁用防重複。
3. **在** 回收 API 成功，**當** 收到 Response，**則** 顯示 Toast「已回收 X 件，獲得 {bonusEarned} Bonus！」，賞品狀態更新為 `RECYCLED`，Header 的 Bonus 餘額更新。
4. **在** 確認 Modal，**當** 玩家點擊「取消」，**則** 關閉 Modal，賞品選取狀態保留。
5. **在** 賞品 `isRecyclable = false`，**當** 玩家勾選，**則** 回收按鈕保持禁用狀態（即使已選其他可回收賞品，若混選不可回收賞品則禁用）。

### 使用者故事 4 — 瀏覽訂單列表（優先級：P2）

玩家在訂單列表頁查看所有歷史訂單。

**驗收情境**：
1. **在** 玩家進入 `/orders`，**當** 頁面載入，**則** 顯示所有訂單卡片，依 `createdAt` 降序排列。
2. **在** 訂單列表，**當** 玩家點擊「待出貨」篩選器，**則** 只顯示 `status = PENDING` 的訂單。
3. **在** 玩家沒有任何訂單，**當** API 返回空陣列，**則** 顯示空狀態插圖與「尚無訂單記錄，快去抽獎吧！」文字，附帶「前往商品頁」按鈕。

---

## API 串接規格

### 賞品盒與訂單

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得賞品盒內容 | GET | `/api/prize-box` | 無 | `id`, `lotteryId`, `lotteryTitle`, `storeId`, `storeName`, `prizeName`, `prizeLevel`, `prizeImageUrl`, `status`, `receivedAt`, `isShippable`, `isRecyclable`, `recycleBonus` | 載入賞品列表 |
| 申請出貨 | POST | `/api/prize-box/ship` | `prizeBoxIds`, `shippingMethod`, ...出貨資訊 | `orderId`, `orderNo`, `status`, `prizes`, `shippingInfo` | 導向訂單詳情 |
| 回收賞品 | POST | `/api/prize-box/recycle` | `prizeBoxIds: [...]` | `bonusEarned`, `newBonusBalance`, `recycledCount` | Toast + 更新餘額 |
| 取得訂單列表 | GET | `/api/orders` | 無 | `id`, `orderNo`, `status`, `statusName`, `storeName`, `prizeCount`, `shippingMethod`, `createdAt` | 訂單卡片列表 |

#### 回收 Response 範例
```json
{
  "bonusEarned": 150,
  "newBonusBalance": 320,
  "recycledCount": 3
}
```

---

## 功能需求（前端 UI）

### 賞品盒頁

- **FR-UI-001**：賞品依 `storeId` 分組，每個店家區塊有獨立的「全選此店家」Checkbox。
- **FR-UI-002**：狀態為 `SHIPPED` 或 `RECYCLED` 的賞品以半透明樣式顯示，Checkbox 禁用。
- **FR-UI-003**：`prizeImageUrl` 為空時顯示預設賞品佔位圖。
- **FR-UI-004**：賞品卡片顯示：賞品圖、賞品名稱、等級標籤（顏色代碼依等級）、店家名稱、入盒時間、狀態標籤。
- **FR-UI-005**：底部固定行動列（Sticky Action Bar）在有賞品被勾選時出現，顯示：已選數量、「申請出貨」按鈕、「回收換分」按鈕。
- **FR-UI-006**：等級顏色代碼：S/SP 級 — 金色；A 級 — 紅色；B 級 — 橙色；C 級 — 綠色；謝謝惠顧 — 灰色。

### 訂單列表頁

- **FR-UI-007**：訂單卡片顯示：訂單號（orderNo）、店家名稱、賞品數量、出貨方式、建立時間、狀態 Badge。
- **FR-UI-008**：狀態 Badge 顏色：待出貨 — 藍色；準備中 — 黃色；已出貨 — 橙色；已完成 — 綠色。

### 狀態管理

- **SM-001**：`prizeBoxItems` — 從 API 載入的完整賞品盒陣列。
- **SM-002**：`selectedPrizeIds` — 已勾選賞品的 UUID Set（使用 Set 避免重複）。
- **SM-003**：`activeStatusTab` — 當前篩選 tab（`ALL` | `IN_BOX` | `SHIPPED` | `RECYCLED`）。
- **SM-004**：`isRecycling` — 控制回收確認 Modal 顯示與 API 呼叫狀態。
- **SM-005**：`orders` — 訂單列表陣列，訂單列表頁專用。

---

## API 驗證清單

- **AV-001** ✅：`GET /api/prize-box` 回傳 `isShippable` 和 `isRecyclable` 布林值，前端可控制 Checkbox 啟用狀態。
- **AV-002** ✅：`POST /api/prize-box/recycle` 回傳 `bonusEarned` 和 `newBonusBalance`，前端可即時更新 Header 餘額。
- **AV-003** ✅：`GET /api/orders` 回傳 `statusName` 中文欄位，可直接顯示在 Badge 上（或前端自行做 i18n mapping）。
- **AV-004** ⚠️：`GET /api/prize-box` 是否支援分頁？若玩家賞品盒有 200+ 賞品，一次性返回效能可能有問題 — 需確認後端是否有 pagination。
- **AV-005** ⚠️：`recycleBonus` 欄位（每個賞品各別的回收價值）是否在 `GET /api/prize-box` 的 Response 中？確認後回收確認 Modal 可顯示明細。
- **AV-006** ❌：`POST /api/prize-box/ship` 的 Response 在多店家拆單時格式不確定（單筆 `orderId` vs 多筆 `orders` 陣列）— 需後端澄清並統一 Response 格式。
- **AV-007** ⚠️：`GET /api/orders` 是否返回 `totalValue` 欄位？訂單卡片若要顯示訂單總值需此欄位。
- **AV-008** ❌：賞品等級（`prizeLevel`）的顏色代碼映射規則需設計規格確認（S/A/B/C 等級定義）。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 成功標準

- **SC-001**：賞品盒頁在 50 筆賞品下首次渲染時間 ≤ 1 秒。
- **SC-002**：勾選/取消勾選賞品的 UI 響應時間 ≤ 50ms（純客戶端狀態更新）。
- **SC-003**：回收成功後，Header 的 Bonus 餘額在 Toast 顯示的同時更新。
- **SC-004**：訂單列表頁的篩選功能為客戶端過濾（不重新呼叫 API），響應時間 ≤ 50ms。
- **SC-005**：從抽獎結果點擊「前往賞品盒」到賞品盒頁面完全載入 ≤ 2 秒（含 API 請求時間）。
