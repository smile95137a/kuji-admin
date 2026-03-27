# 前端規格書（前台）：賞品盒

**功能分支**：`cli/010-prize-box`
**對應後端 Spec**：`specs/010-prize-box/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：需登入（ROLE_USER）

---

## 頁面與介面清單

### 頁面 1 — 賞品盒頁
- **路由**：`/prize-box`
- **存取權限**：需登入
- **UI 元件**：
  - 狀態 Tab 篩選（全部 / 在賞品盒 / 已出貨 / 已回收）
  - 賞品列表（依店家分組，Group Header 含店家名稱 + 全選按鈕）
  - 賞品卡片（圖片、名稱、等級標籤、回收 Bonus 顯示、入盒時間、狀態）
  - 多選 Checkbox（IN_BOX 且 isShippable/isRecyclable 時啟用）
  - 固定底部行動列（已選 X 件｜申請出貨按鈕｜回收換分按鈕）
  - 多店家拆單警告橫幅（選取跨店家賞品時顯示）
  - 賞品盒為空時的空狀態插圖

### 頁面 2 — 出貨確認 Modal / 頁面
- **路由**：`/prize-box/ship`（手機全頁）或 Modal（桌機）
- **UI 元件**：
  - 已選賞品摘要
  - 出貨方式 Radio 選擇器（宅配到府 / 超商取貨）
  - 已儲存地址下拉選單
  - 收件表單（動態切換宅配/超商欄位）
  - 提交按鈕
  - 多店家拆單說明

### 頁面 3 — 回收確認 Modal
- **路由**：`/prize-box`（overlay）
- **UI 元件**：
  - 待回收賞品列表（縮略圖 + 名稱 + 各別 Bonus）
  - 預計獲得 Bonus 合計（大字體金色顯示）
  - 不可逆警告文字（紅色）
  - 確認回收按鈕
  - 取消按鈕

---

## 使用者情境與測試

### 使用者故事 1 — 瀏覽賞品盒並管理賞品（優先級：P1）

玩家進入賞品盒，查看所有已獲得的賞品，並依狀態篩選。

**此優先級的原因**：賞品盒是玩家抽獎後的唯一賞品儲存地點，直接關係到出貨與回收的用戶旅程。

**獨立測試**：
1. 以有賞品記錄的玩家進入 `/prize-box`。
2. 確認賞品依店家分組顯示。

**驗收情境**：
1. **在** 玩家進入 `/prize-box`，**當** `GET /api/prize-box` 回應，**則** 賞品以店家為單位分組顯示，每個店家區塊顯示店家名稱（標頭）與屬於該店家的賞品列表。
2. **在** 狀態 Tab，**當** 玩家點擊「在賞品盒（IN_BOX）」，**則** 客戶端過濾只顯示 `status = IN_BOX` 的賞品，其他狀態的賞品隱藏。
3. **在** 賞品卡片，**當** 顯示 `IN_BOX` 賞品，**則** 顯示：賞品圖片（或佔位圖）、賞品名稱、等級標籤（顏色代碼）、回收可獲 Bonus（`recycleBonus` 點）、入盒時間。
4. **在** 賞品盒為空（無任何賞品），**當** API 返回空陣列，**則** 顯示空狀態插圖（禮物盒圖示）與「賞品盒是空的，去抽獎吧！」，附「前往商品頁」按鈕。
5. **在** 賞品 `status = SHIPPED` 或 `RECYCLED`，**當** 賞品卡片顯示，**則** 以半透明樣式呈現，Checkbox 禁用，顯示對應狀態標籤。

### 使用者故事 2 — 多選賞品並申請出貨（優先級：P1）

玩家在賞品盒勾選一或多個 IN_BOX 賞品，點擊「申請出貨」。

**驗收情境**：
1. **在** 玩家勾選 3 個 `IN_BOX` 且 `isShippable = true` 的賞品，**當** 勾選後，**則** 底部固定行動列出現，顯示「已選 3 件｜申請出貨｜回收換分」。
2. **在** 玩家點擊店家區塊的「全選」按鈕，**當** 點擊，**則** 該店家所有 `IN_BOX` 且 `isShippable = true` 的賞品全部選中。
3. **在** 已選賞品來自 2 個不同店家，**當** 點擊「申請出貨」，**則** 顯示警告橫幅「您選取的賞品來自 2 個店家，將自動拆分為 2 筆出貨訂單，請確認」，玩家點擊「確認繼續」後才開啟出貨表單。
4. **在** 出貨表單提交成功，**當** API 回應訂單資料，**則** 關閉 Modal，顯示 Toast「出貨申請成功！訂單號：{orderNo}」，已出貨的賞品狀態更新為 `SHIPPED`（樂觀更新）。
5. **在** 選取的賞品中有 `isShippable = false` 的項目，**當** 勾選，**則** Checkbox 禁用，Tooltip 顯示「此賞品無法出貨」。

### 使用者故事 3 — 回收賞品換取 Bonus（優先級：P1）

玩家選取可回收賞品，確認後換取 Bonus 點數。

**驗收情境**：
1. **在** 玩家勾選 `isRecyclable = true` 的賞品，**當** 點擊「回收換分」，**則** 顯示回收確認 Modal，列出待回收賞品清單。
2. **在** 回收確認 Modal，**當** 顯示，**則** 醒目顯示預計獲得 Bonus 合計（所有選中賞品的 `recycleBonus` 加總），金色大字體。
3. **在** 回收確認 Modal，**當** 顯示，**則** 顯示不可逆警告「⚠️ 回收後無法復原，賞品將永久消失」紅色文字。
4. **在** 玩家點擊「確認回收」，**當** API 回應成功，**則** 顯示 Toast「🎉 回收成功！獲得 {bonusEarned} Bonus 點」，Header 的 Bonus 餘額即時更新，賞品狀態更新為 `RECYCLED`。
5. **在** 選取賞品中混有 `isRecyclable = false` 的項目，**當** 點擊「回收換分」，**則** 按鈕禁用或顯示提示「部分選取賞品不可回收，請重新選擇」。

### 使用者故事 4 — 預設地址自動帶入出貨表單（優先級：P2）

玩家開啟出貨表單時，自動帶入預設地址。

**驗收情境**：
1. **在** 出貨表單開啟，**當** `GET /api/address/default` 回應，**則** 收件人姓名、電話、地址欄位自動填入預設地址資料。
2. **在** 已儲存地址下拉選單，**當** 玩家選擇其他已儲存地址，**則** 表單欄位即時更新為所選地址資料。
3. **在** 玩家無儲存地址，**當** 表單開啟，**則** 表單欄位保持空白，顯示 Placeholder 引導輸入。

---

## API 串接規格

### 賞品盒核心

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得賞品盒內容 | GET | `/api/prize-box` | 無 | `id`, `lotteryId`, `lotteryTitle`, `storeId`, `storeName`, `prizeName`, `prizeLevel`, `prizeImageUrl`, `recycleBonus`, `status`, `receivedAt`, `isShippable`, `isRecyclable` | 載入賞品列表 |
| 申請出貨 | POST | `/api/prize-box/ship` | `prizeBoxIds[]`, `shippingMethod`, 出貨相關欄位 | `orders: [{orderId, orderNo, storeName, prizeCount}]` | Toast + 更新賞品狀態 |
| 回收賞品 | POST | `/api/prize-box/recycle` | `prizeBoxIds: [...]` | `bonusEarned`, `newBonusBalance`, `recycledCount` | Toast + 更新餘額 + 賞品狀態 |
| 取得已儲存地址 | GET | `/api/address` | 無 | `id`, `recipientName`, `recipientPhone`, `address`, `isDefault`, `addressType` | 出貨表單下拉選單 |
| 取得預設地址 | GET | `/api/address/default` | 無 | `recipientName`, `recipientPhone`, `address` | 出貨表單預填 |

---

## 功能需求（前端 UI）

### 賞品盒布局

- **FR-UI-001**：賞品依 `storeId` 分組，每組區塊有標題列（店家 Logo + 名稱 + 全選 Checkbox + 已選數量）。
- **FR-UI-002**：賞品卡片在手機為 2 欄格子、桌機為 4 欄格子。
- **FR-UI-003**：賞品卡片依 `receivedAt` 降序排列（最新獲得的在最前面）。
- **FR-UI-004**：等級標籤顏色：S/SP — 金色、A — 紅色、B — 橙色、C — 綠色、謝謝惠顧（無 prizeLevel）— 灰色。
- **FR-UI-005**：新獲得的賞品（在最近 24 小時內）顯示「NEW」標籤（客戶端判斷：`receivedAt` 與當前時間差 < 24 小時）。

### 固定行動列（Sticky Action Bar）

- **FR-UI-006**：底部 Sticky 行動列在 `selectedPrizeIds` 非空時出現，有入場動畫（slide up）。
- **FR-UI-007**：行動列顯示：「已選 X 件」+ 「申請出貨」按鈕（有 `isShippable` 賞品時啟用）+ 「回收換分」按鈕（有 `isRecyclable` 賞品時啟用）。
- **FR-UI-008**：當選中賞品中無任何 `isShippable = true` 的項目時，「申請出貨」按鈕禁用並顯示 Tooltip 說明原因。

### 狀態管理

- **SM-001**：`prizeBoxItems` — 賞品盒原始陣列（來自 API）。
- **SM-002**：`selectedPrizeIds` — 已勾選賞品 UUID 的 Set。
- **SM-003**：`activeStatusTab` — 當前 Tab（`ALL` | `IN_BOX` | `SHIPPED` | `RECYCLED`）。
- **SM-004**：`groupedPrizes` — 計算屬性：依 `storeId` 分組的賞品 Map，供渲染使用。
- **SM-005**：`isRecycleModalOpen` — 回收確認 Modal 顯示狀態。
- **SM-006**：`isShipModalOpen` — 出貨表單 Modal 顯示狀態。
- **SM-007**：`estimatedBonusTotal` — 計算屬性：所有選中賞品的 `recycleBonus` 加總，用於回收 Modal 顯示。

---

## API 驗證清單

- **AV-001** ✅：`GET /api/prize-box` 回傳 `isShippable` 和 `isRecyclable`，可控制 Checkbox 啟用狀態 — 已確認。
- **AV-002** ✅：`GET /api/prize-box` 回傳 `recycleBonus`（每個賞品個別回收價值），可加總顯示預計 Bonus — 已確認。
- **AV-003** ✅：`POST /api/prize-box/recycle` 回傳 `bonusEarned` 和 `newBonusBalance`，可即時更新 Header 餘額 — 已確認。
- **AV-004** ⚠️：`POST /api/prize-box/ship` 的 Response 格式：規格顯示 `{ orders: [{ orderId, orderNo, storeName, prizeCount }] }` 為陣列，但 002-express-shipping 規格中顯示為單筆 `{ orderId, orderNo, ... }` — 需後端統一格式（建議統一為陣列支持多店拆單）。
- **AV-005** ⚠️：`GET /api/address/default` 端點是否存在？010-prize-box 規格中提及，但 002-express-shipping 只有 `GET /api/address` — 需確認。
- **AV-006** ❌：`GET /api/prize-box` Response 缺少分頁支援 — 若玩家有大量賞品（>200件），一次性返回影響效能，建議後端支援分頁或無限捲動。
- **AV-007** ⚠️：賞品狀態更新（申請出貨/回收後）是否使用樂觀更新（Optimistic Update）？若使用，API 失敗後需回滾 UI 狀態。
- **AV-008** ❌：`prizeLevel` 的可能值（如 "S", "SP", "A", "B", "C"）沒有統一定義文件 — 顏色代碼 mapping 需前後端對齊，建議後端補充枚舉定義。
- **AV-009** ⚠️：多店家出貨時，`POST /api/prize-box/ship` 的 Request 中不需要特別標記「這是多店家出貨」嗎？後端是否自動按 storeId 拆單？前端需確認後端自動拆單邏輯，避免雙重警告。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 成功標準

- **SC-001**：賞品盒頁在 50 件賞品下首次渲染 ≤ 1.5 秒（含 API 回應）。
- **SC-002**：勾選/取消勾選賞品 UI 響應 ≤ 50ms（純客戶端狀態更新）。
- **SC-003**：回收成功後，Header Bonus 餘額在 Toast 出現的同時更新，不需要重整頁面。
- **SC-004**：多店家警告橫幅在選取第二個不同店家賞品後立即顯示（< 100ms）。
- **SC-005**：底部固定行動列的入場動畫流暢（無掉幀），時長 200ms。
- **SC-006**：回收確認 Modal 的預計 Bonus 合計計算正確，與 API 回傳的 `bonusEarned` 誤差為零（客戶端計算與後端計算一致）。
