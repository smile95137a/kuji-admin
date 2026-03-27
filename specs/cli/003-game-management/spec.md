# 前端規格書（前台）：遊戲抽獎機制

**功能分支**：`cli/003-game-management`
**對應後端 Spec**：`specs/003-game-management/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：需登入（ROLE_USER）

---

## 頁面與介面清單

### 頁面 1 — 商品抽獎頁
- **路由**：`/lottery/{id}/draw`
- **存取權限**：需登入
- **UI 元件**：
  - 票券格子（Ticket Grid — 顯示所有票券的 ticketNumber）
  - 已抽票券覆蓋層（Drawn Ticket Overlay — 灰色/帶賞品圖示）
  - 抽獎按鈕（Draw Button — 含點數費用顯示）
  - 保護時間倒數計時器（Protection Timer Banner）
  - 點數餘額顯示（Gold / Bonus Balance in Header）
  - 商品資訊側欄（Lottery Info Sidebar）
  - 一番賞大賞指定標籤（Grand Prize Badge on specific tickets）

### 頁面 2 — 抽獎結果 Modal
- **路由**：`/lottery/{id}/draw`（overlay）
- **UI 元件**：
  - 獎品動畫（Prize Reveal Animation）
  - 獎品名稱與等級標籤
  - 獎品圖片
  - 獲得點數說明（如：謝謝惠顧 — 返還 bonus）
  - 繼續抽按鈕
  - 前往賞品盒按鈕
  - 查看結果摘要按鈕

---

## 使用者情境與測試

### 使用者故事 1 — 執行隨機抽獎（優先級：P1）

玩家在一番賞/扭蛋/卡牌商品頁點擊「抽一次」，系統隨機消耗點數並返回獎品。

**此優先級的原因**：抽獎是平台核心功能，整個商業模式依賴此流程。

**獨立測試**：
1. 確認 `GET /api/lottery/draw/{lotteryId}/tickets` 返回有 `AVAILABLE` 票券的商品。
2. 確認玩家有足夠 goldCoins 餘額。
3. 點擊抽獎按鈕，驗證結果 Modal 顯示。

**驗收情境**：
1. **在** 票券格子顯示，**當** 玩家點擊「抽一次」按鈕，**則** 呼叫 `POST /api/lottery/draw/{lotteryId}/draw`（body: `{ count: 1 }`），按鈕立即禁用防止重複。
2. **在** API 回應成功，**當** 收到抽獎結果陣列，**則** 觸發獎品揭示動畫（≤ 2 秒），動畫結束後顯示結果 Modal。
3. **在** 結果 Modal 顯示，**當** 玩家看到獎品，**則** Modal 顯示：賞品名稱、等級、圖片、消耗點數、剩餘餘額（`remainingGold` + `remainingBonus`）。
4. **在** 抽獎完成，**當** 玩家點擊「繼續抽」，**則** 關閉 Modal，返回票券格子，已抽票券顯示灰色覆蓋。
5. **在** 玩家 goldCoins 不足，**當** 點擊抽獎按鈕，**則** 顯示「點數不足，請先儲值」提示，按鈕禁用，不呼叫 API。

### 使用者故事 2 — 指定票券抽獎（優先級：P1）

玩家在票券格子中點選特定票券號碼後進行抽獎（一番賞/扭蛋模式）。

**此優先級的原因**：指定抽是差異化遊玩體驗，玩家策略性選擇大賞所在位置。

**獨立測試**：點擊票券格子中的某個號碼使其被選中，確認按鈕文字顯示所選號碼。

**驗收情境**：
1. **在** 票券格子，**當** 玩家點擊某個 `AVAILABLE` 票券，**則** 票券以高亮樣式顯示（選中狀態），抽獎按鈕文字更新為「抽 #[ticketNumber]」。
2. **在** 玩家已選中票券，**當** 點擊「抽 #XX」，**則** Request body 包含 `{ count: 1, ticket: ["ticket-uuid"] }`。
3. **在** 玩家點擊已選中的票券，**當** 再次點擊，**則** 取消選中，恢復隨機模式按鈕文字。
4. **在** 票券狀態為 `DRAWN`，**當** 玩家嘗試點擊，**則** 票券不可選中，顯示已抽賞品資訊 Tooltip。

### 使用者故事 3 — 保護時間鎖定（優先級：P1）

新開套商品有保護時間，保護時間內非開套者無法抽獎。

**此優先級的原因**：保護機制是重要的公平性設計，必須在前端明確顯示並強制執行。

**驗收情境**：
1. **在** `session.isOpener = false` 且 `protectionEndTime` 未到期，**當** 玩家進入抽獎頁，**則** 抽獎按鈕禁用，顯示保護倒數計時器「保護時間剩餘 HH:MM:SS」。
2. **在** 保護倒數計時器顯示，**當** 倒數歸零，**則** 按鈕自動解鎖，倒數器隱藏，不需要重新整理頁面。
3. **在** `session.isOpener = true`，**當** 玩家進入抽獎頁，**則** 保護計時器不顯示，玩家可立即抽獎。
4. **在** `session.grandPrizesDesignated = false` 且 `isOpener = true`（刮刮樂模式），**當** 頁面載入，**則** 顯示提示「請先指定大賞位置才能讓其他玩家抽獎」並跳轉至指定頁面。

### 使用者故事 4 — 熱門計數觸發（優先級：P3）

玩家進入商品詳情頁時，自動增加該商品的熱門瀏覽數。

**驗收情境**：
1. **在** 玩家進入 `/lottery/{id}`，**當** 頁面 `onMounted`，**則** 靜默呼叫 `POST /api/lottery/browse/{lotteryId}/hot`（不阻塞頁面載入，不顯示任何 UI 反饋）。

---

## API 串接規格

### 抽獎核心

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得票券列表 | GET | `/api/lottery/draw/{lotteryId}/tickets` | 無 | `tickets[]`, `session.isOpener`, `session.protectionEndTime`, `session.grandPrizesDesignated` | 載入票券格子 |
| 執行抽獎 | POST | `/api/lottery/draw/{lotteryId}/draw` | `count`, `ticket`(optional) | `ticketNumber`, `prizeId`, `prizeName`, `prizeLevel`, `prizeImageUrl`, `isGrandPrize`, `isFinalPrize`, `pointsConsumed`, `remainingGold`, `remainingBonus` | 動畫 → 結果 Modal |
| 指定大賞位置（刮刮樂） | POST | `/api/lottery/draw/{lotteryId}/designate` | `prizeNumbers: [...]` | `success`, `designatedCount` | 成功提示 → 解鎖其他玩家 |
| 增加熱門計數 | POST | `/api/lottery/browse/{lotteryId}/hot` | `increment: 1` | 無（忽略） | 靜默呼叫 |

#### Draw Response 範例（注意：永遠是陣列格式）
```json
[
  {
    "ticketNumber": 42,
    "prizeId": "prize-uuid",
    "prizeName": "A賞 — 鋼彈 RX-78-2",
    "prizeLevel": "A",
    "prizeImageUrl": "https://cdn.example.com/prizes/a-prize.jpg",
    "isGrandPrize": false,
    "isFinalPrize": false,
    "pointsConsumed": 300,
    "remainingGold": 1700,
    "remainingBonus": 50
  }
]
```

**重要**：`prizeId` 在刮刮樂謝謝惠顧時為 `null`。

---

## 功能需求（前端 UI）

### 票券格子

- **FR-UI-001**：票券格子依 `ticketNumber` 升序排列，每格顯示號碼，`AVAILABLE` 為淺色、`DRAWN` 為深灰色並加蒙層。
- **FR-UI-002**：`AVAILABLE` 票券不得顯示 `prizeId`、`prizeLevel`，後端不應返回此資訊（安全需求）；前端即使收到也不渲染。
- **FR-UI-003**：大賞（`isGrandPrize`）票券在指定後顯示特殊標記（皇冠圖示），但僅開套者可見。
- **FR-UI-004**：票券格子在手機版為 5 列，桌機版為 10 列，超過 50 張時增加「顯示更多」折疊。

### 抽獎動畫

- **FR-UI-005**：抽獎結果動畫時長 1.5 秒，顯示閃光或翻牌效果，期間全螢幕遮罩防止誤觸。
- **FR-UI-006**：大賞（`isGrandPrize = true`）觸發特殊慶祝動畫（煙火或彩帶效果），時長 3 秒。
- **FR-UI-007**：謝謝惠顧（`prizeId = null`）顯示安慰獎圖示，文字「謝謝惠顧」，不顯示賞品圖。
- **FR-UI-008**：最終獎（`isFinalPrize = true`）額外顯示「🎉 最後一張！」橫幅，並顯示商品已完售提示。

### 狀態管理

- **SM-001**：`tickets` — 票券陣列，從 API 載入，抽獎後更新對應票券 status。
- **SM-002**：`sessionInfo` — `{ isOpener, protectionEndTime, grandPrizesDesignated }`。
- **SM-003**：`isDrawing` — 防止重複點擊，控制動畫播放。
- **SM-004**：`selectedTicketId` — 玩家選中的票券 UUID（null 表示隨機模式）。
- **SM-005**：`protectionCountdown` — 使用 `setInterval` 每秒更新的倒數秒數。
- **SM-006**：`lastDrawResult` — 最後一次抽獎結果陣列，供結果 Modal 顯示。

---

## API 驗證清單

- **AV-001** ✅：Draw Response 永遠是陣列格式（即使單次抽獎）— 前端需用 `result[0]` 取第一個元素。
- **AV-002** ✅：`remainingGold` 和 `remainingBonus` 在 Draw Response 中返回，前端可即時更新 Header 餘額顯示。
- **AV-003** ✅：`session.protectionEndTime` 為 ISO 8601 時間字串，前端計算剩餘秒數。
- **AV-004** ⚠️：`GET .../tickets` Response 中 `AVAILABLE` 票券是否確保不含 `prizeId`/`prizeLevel`？需後端確認安全過濾。
- **AV-005** ⚠️：`session.isOpener` 的判斷依據是什麼？（第一個對此 lottery 下注的玩家？）需確認語意。
- **AV-006** ❌：Draw Response 缺少 `newBalance` 物件（只有 `remainingGold`/`remainingBonus` 個別欄位）— 確認 Header 錢包餘額是讀此兩欄位更新，還是需要重新呼叫 `/api/user/me`。
- **AV-007** ⚠️：當商品 `maxDraws` 已達上限（即 `isFinalPrize = true`），後端是否自動將商品狀態更改為 `COMPLETED`？前端需知道何時禁用抽獎按鈕。
- **AV-008** ❌：`GET .../tickets` 未返回商品基本資訊（`title`, `pricePerDraw`）— 抽獎頁需要顯示商品名稱和每抽費用，需另外呼叫 `GET /api/lottery/browse/{id}` 或在 tickets API 中補充。
- **AV-009** ⚠️：`designate` API 的 `prizeNumbers` 是「顯示號碼（revealedNumber）」還是「票券序號（ticketNumber）」？需明確區分。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 成功標準

- **SC-001**：抽獎按鈕點擊到動畫開始 ≤ 500ms（感知延遲）。
- **SC-002**：票券格子在 100 張票券時渲染時間 ≤ 300ms（使用虛擬化或分頁）。
- **SC-003**：保護時間倒數計時器每秒更新，誤差 ≤ 100ms。
- **SC-004**：抽獎結果 Modal 在動畫完成後 100ms 內顯示。
- **SC-005**：餘額顯示在抽獎結果返回後立即更新（使用 Response 中的 `remainingGold`/`remainingBonus`）。
- **SC-006**：玩家在沒有足夠點數時，嘗試點擊抽獎按鈕能在 0ms 內看到提示（客戶端驗證，不等待 API）。
