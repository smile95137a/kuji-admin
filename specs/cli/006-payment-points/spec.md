# 前端規格書（前台）：付款與點數系統

**功能分支**：`cli/006-payment-points`
**對應後端 Spec**：`specs/006-payment-points/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：需登入（ROLE_USER）

---

## 頁面與介面清單

### 頁面 1 — 錢包總覽頁
- **路由**：`/wallet`
- **存取權限**：需登入
- **UI 元件**：
  - 金幣餘額卡片（Gold Coins Balance Card）
  - 獎勵幣餘額卡片（Bonus Coins Balance Card）
  - 累計儲值金額顯示（Total Recharged）
  - 「立即儲值」CTA 按鈕（導向 `/wallet/topup`）
  - 交易記錄快捷列表（最近 5 筆）
  - 「查看全部記錄」連結
  - Tab 切換（交易記錄 / 消費記錄）

### 頁面 2 — 儲值頁
- **路由**：`/wallet/topup`
- **存取權限**：需登入
- **UI 元件**：
  - 儲值方案卡片格子（Recharge Plan Cards）
  - 已選方案高亮效果
  - 付款方式選擇（信用卡）
  - 金額明細顯示（獲得金幣 + 獎勵幣）
  - 「確認付款」按鈕
  - 付款成功 Modal

### 頁面 3 — 交易記錄頁
- **路由**：`/wallet/transactions`
- **存取權限**：需登入
- **UI 元件**：
  - 交易記錄列表
  - 交易類型篩選器（全部 / 儲值 / 抽獎 / 回收 / 調整 / 出貨）
  - 每筆記錄：類型圖示、原因說明、金幣變化、獎勵幣變化、時間、交易後餘額
  - 分頁或無限捲動

### 頁面 4 — 消費記錄頁
- **路由**：`/wallet/consumption`
- **存取權限**：需登入
- **UI 元件**：
  - 消費列表（抽獎 / 出貨費用）
  - 消費類型篩選（DRAW / SHIPPING）
  - 每筆記錄：類型、消耗金幣、消耗獎勵幣、相關商品/訂單、時間

---

## 使用者情境與測試

### 使用者故事 1 — 查看錢包餘額（優先級：P1）

玩家進入錢包頁，清楚看到金幣與獎勵幣各別餘額。

**此優先級的原因**：點數餘額是玩家在抽獎前必須確認的核心資訊，顯示錯誤會直接影響信任感。

**獨立測試**：
1. 以已登入玩家進入 `/wallet`。
2. 確認金幣與獎勵幣顯示與 `GET /api/wallet` 或 `GET /api/user/me` 回傳值一致。

**驗收情境**：
1. **在** 玩家進入 `/wallet`，**當** API 載入完成，**則** 顯示金幣（goldCoins）和獎勵幣（bonusCoins）餘額，以千分位格式顯示（如 1,500）。
2. **在** 錢包頁，**當** 玩家剛完成一筆抽獎後返回，**則** 餘額顯示最新數值（不需手動重新整理）。
3. **在** 金幣餘額為 0，**當** 頁面顯示，**則** 餘額顯示「0」（不顯示負數），「立即儲值」按鈕以強調色突顯。
4. **在** Header/Navbar，**當** 玩家在任何頁面，**則** Header 顯示金幣餘額（小金幣圖示 + 數字），點擊導向 `/wallet`。

### 使用者故事 2 — 選擇儲值方案並付款（優先級：P1）

玩家在儲值頁選擇方案，點擊確認後完成付款，餘額立即更新。

**此優先級的原因**：儲值是平台核心商業模式，付款流程的每個步驟都需要完善。

**獨立測試**：
1. 進入 `/wallet/topup`，確認方案卡片顯示。
2. 選擇一個方案，點擊「確認付款」。
3. 確認付款成功 Modal 顯示，餘額更新。

**驗收情境**：
1. **在** 儲值頁載入，**當** `GET /api/recharge/plans` 回應，**則** 所有 `isActive = true` 的方案以卡片形式顯示，依金額升序排列。
2. **在** 方案卡片顯示，**當** 玩家點選某個方案，**則** 卡片高亮（選中狀態），右側顯示方案明細（獲得金幣 + 獎勵幣 + 費用）。
3. **在** 玩家點擊「確認付款」，**當** 按鈕啟用（已選方案），**則** 呼叫 `POST /api/recharge`，按鈕禁用防重複。
4. **在** 付款 API 成功（不需外部跳轉），**當** 收到 `{ goldAdded, bonusAdded, newBalance }`，**則** 顯示「儲值成功！獲得 {goldAdded} 金幣 + {bonusAdded} 獎勵幣」Modal，Header 餘額即時更新。
5. **在** 付款 API 返回 `paymentUrl`（需外部跳轉），**當** 收到，**則** 以新視窗或當前頁面導向 `paymentUrl`。
6. **在** 未選擇方案，**當** 玩家點擊「確認付款」，**則** 按鈕保持禁用（或顯示「請先選擇儲值方案」提示）。

### 使用者故事 3 — 查看交易記錄（優先級：P2）

玩家查看歷史交易記錄，了解每筆點數增減原因。

**驗收情境**：
1. **在** 交易記錄頁，**當** 頁面載入，**則** 顯示所有交易記錄，依 `createdAt` 降序排列。
2. **在** 記錄列表，**當** 玩家篩選「儲值」，**則** 只顯示 `type = RECHARGE` 的記錄。
3. **在** 每筆交易記錄，**當** 顯示，**則** 包含：類型圖示、原因（`reason`）、金幣變化（正數綠色 +XXX，負數紅色 -XXX）、獎勵幣變化、時間。
4. **在** 交易記錄有關聯訂單（`relatedOrderNo` 不為空），**當** 玩家點擊記錄，**則** 顯示「查看訂單」連結，導向 `/orders/{orderId}`。

### 使用者故事 4 — 查看消費記錄（優先級：P2）

玩家查看抽獎與出貨的消費明細。

**驗收情境**：
1. **在** 消費記錄頁，**當** 頁面載入，**則** 顯示抽獎（DRAW）和出貨（SHIPPING）的消費記錄。
2. **在** 消費記錄，**當** 顯示抽獎記錄，**則** 顯示商品名稱（`relatedTitle`）、消耗金幣（`goldSpent`）、消耗獎勵幣（`bonusSpent`）。
3. **在** 點擊消費記錄，**當** 有 `relatedId`，**則** 可連結至對應商品頁或訂單頁。

---

## API 串接規格

### 錢包與點數

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得使用者資訊（含餘額）| GET | `/api/user/me` | 無 | `goldCoins`, `bonusCoins`, `totalRecharged`, `nickname`, `avatar` | 初始化錢包餘額 |
| 取得錢包詳情 | GET | `/api/wallet` | 無 | `goldCoins`, `bonusCoins`, `totalRecharged`, `userNickname`, `userEmail` | 錢包頁顯示 |
| 取得交易記錄 | GET | `/api/wallet/transactions` | 無 | `id`, `type`, `goldChange`, `bonusChange`, `reason`, `relatedOrderNo`, `createdAt`, `balanceAfterGold`, `balanceAfterBonus` | 交易記錄列表 |
| 取得儲值方案 | GET | `/api/recharge/plans` | 無 | `id`, `goldAmount`, `bonusAmount`, `price`, `isActive`, `description` | 方案卡片格子 |
| 執行儲值 | POST | `/api/recharge` | `planId`, `paymentMethod` | `transactionId`, `goldAdded`, `bonusAdded`, `newBalance.goldCoins`, `newBalance.bonusCoins`, `paymentUrl`(optional) | 付款成功 Modal |
| 取得消費記錄 | GET | `/api/consumption` | 無 | `id`, `type`, `goldSpent`, `bonusSpent`, `relatedId`, `relatedTitle`, `createdAt` | 消費記錄列表 |

#### 儲值方案 Response 範例
```json
[
  {
    "id": "plan-uuid-1",
    "goldAmount": 500,
    "bonusAmount": 50,
    "price": 150,
    "isActive": true,
    "description": "入門方案"
  },
  {
    "id": "plan-uuid-2",
    "goldAmount": 1000,
    "bonusAmount": 150,
    "price": 300,
    "isActive": true,
    "description": "超值方案 — 額外贈 150 獎勵幣"
  }
]
```

---

## 功能需求（前端 UI）

### 儲值方案卡片

- **FR-UI-001**：每張方案卡片顯示：費用（`price` 元）、獲得金幣數、獲得獎勵幣數、描述文字。
- **FR-UI-002**：最熱門 / 推薦方案（由描述或特定標記判斷）顯示「推薦」Badge。
- **FR-UI-003**：`isActive = false` 的方案不顯示在列表中。
- **FR-UI-004**：方案卡片在手機為單列，桌機為 3 列格子。

### 餘額全域顯示

- **FR-UI-005**：Header/Navbar 顯示金幣圖示與餘額，登入後可見，未登入隱藏。
- **FR-UI-006**：餘額使用全域狀態管理（Pinia / Zustand / Redux），所有頁面共享同一份狀態。
- **FR-UI-007**：每次抽獎（使用 Draw Response 的 `remainingGold`/`remainingBonus`）、儲值（使用 Recharge Response 的 `newBalance`）後主動更新全域餘額狀態，無需重新呼叫 API。

### 交易記錄展示

- **FR-UI-008**：金幣/獎勵幣變化正數顯示為綠色「+XXX」，負數顯示為紅色「-XXX」。
- **FR-UI-009**：交易類型對應圖示：RECHARGE — 💰、DRAW — 🎯、RECYCLE — ♻️、ADJUST — ⚙️、SHIPPING — 📦。
- **FR-UI-010**：`balanceAfterGold` 顯示為「交易後餘額」，幫助玩家理解點數變化脈絡。

### 狀態管理

- **SM-001**：`goldCoins` / `bonusCoins` — 全域狀態，整個應用共享。
- **SM-002**：`selectedPlanId` — 儲值頁選中方案 ID。
- **SM-003**：`isPaymentProcessing` — 控制付款按鈕禁用。
- **SM-004**：`transactionFilter` — 當前選擇的交易類型篩選器值。
- **SM-005**：`transactions` / `consumptions` — 各自的記錄陣列（客戶端過濾）。

### 點數使用優先順序

- **FR-UI-011**：介面說明欄顯示「抽獎時優先使用金幣，金幣不足時使用獎勵幣補足」。
- **FR-UI-012**：儲值頁顯示金幣與獎勵幣的差異說明（金幣：可出貨；獎勵幣：僅限抽獎）。

---

## API 驗證清單

- **AV-001** ⚠️：`GET /api/user/me` 與 `GET /api/wallet` 都返回 `goldCoins`/`bonusCoins`，前端應統一使用一個來源 — 建議只呼叫 `/api/user/me` 取得全部資訊，避免兩個 API 資料不同步。（錢包架構變更：goldCoins/bonusCoins 現在在 user 表，不在獨立 wallet 表）
- **AV-002** ✅：`POST /api/recharge` Response 包含 `newBalance.goldCoins` 和 `newBalance.bonusCoins`，前端可即時更新全域狀態。
- **AV-003** ✅：`GET /api/wallet/transactions` 包含 `balanceAfterGold` 和 `balanceAfterBonus`，可顯示交易後餘額。
- **AV-004** ⚠️：`GET /api/wallet/transactions` 是否支援分頁（`page`/`size` 參數）？若交易記錄龐大，無分頁會造成效能問題。
- **AV-005** ❌：`GET /api/consumption` 與 `GET /api/wallet/transactions` 有重疊（抽獎/出貨記錄同時在兩者出現） — 前端需釐清兩者定位差異，避免玩家困惑（建議後端文件說明）。
- **AV-006** ⚠️：`POST /api/recharge` 中 `paymentMethod` 目前只支援 `"CREDIT_CARD"`，是否有其他支付方式規劃？前端是否需要顯示多個選項。
- **AV-007** ❌：`POST /api/recharge` Response 的 `paymentUrl` 是否為必需欄位？若付款在後端直接完成（測試模式），`paymentUrl` 為 null — 前端需處理兩種情境。
- **AV-008** ⚠️：儲值方案是否有「限時方案」機制（帶有 `endTime` 欄位）？若有，前端需顯示倒數計時。
- **AV-009** ❌：`GET /api/recharge/plans` Response 缺少排序欄位（如 `sortOrder`） — 前端無法確定方案顯示順序，建議後端補充或按 `price` 升序。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 成功標準

- **SC-001**：錢包頁載入（含 API 呼叫）時間 ≤ 1.5 秒（P90）。
- **SC-002**：儲值成功後，Header 餘額更新無需重整，延遲 ≤ 200ms。
- **SC-003**：儲值方案在 `/wallet/topup` 載入時間 ≤ 1 秒。
- **SC-004**：交易記錄客戶端篩選響應 ≤ 50ms。
- **SC-005**：付款按鈕在 API 回應前持續禁用，避免重複付款。
- **SC-006**：金幣與獎勵幣以千分位逗號格式顯示（如 10,000），避免大數字閱讀困難。
