# 前端規格書（前台）：商品與抽獎

**功能分支**：`cli/011-product-lottery`
**對應後端 Spec**：`specs/011-product-lottery/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：公開瀏覽（商品列表/詳情）；需登入（執行抽獎）

---

## 頁面與介面清單

### 頁面 1 — 商品列表頁
- **路由**：`/lottery`
- **存取權限**：公開
- **UI 元件**：
  - 分類 Tab 篩選（全部 / 一番賞 / 扭蛋 / 刮刮樂 / 卡牌）
  - 關鍵字搜尋框
  - 排序選擇器（最熱門 / 最新 / 剩餘最多 / 剩餘最少）
  - 商品卡片格子（Product Card Grid）
  - 每張商品卡片：封面圖、商品名稱、店家名稱、每抽金額、剩餘張數/進度條、分類標籤、遊戲模式標籤
  - 即將售完橫幅（剩餘 < 10%）
  - 空狀態插圖（無商品時）
  - 無限捲動（Infinite Scroll）或「載入更多」

### 頁面 2 — 商品詳情頁
- **路由**：`/lottery/{id}`
- **存取權限**：公開（瀏覽）；需登入（抽獎）
- **UI 元件**：
  - 商品封面大圖（含商品名稱 overlay）
  - 商品資訊欄（店家名稱、分類、遊戲模式、每抽金額、剩餘張數）
  - 開賣倒數計時器（未開賣時）
  - 抽獎 CTA 按鈕（「立即抽獎 — XXX 金幣」或「即將開賣」或「已售完」）
  - 獎品列表（Prize Pool Display）
  - 商品說明富文本（description）
  - 返回列表按鈕

### 頁面 3 — 抽獎頁
- **路由**：`/lottery/{id}/draw`
- **存取權限**：需登入
- **UI 元件**：（詳見 003-game-management 規格）
  - 票券格子
  - 抽獎按鈕
  - 保護計時器
  - 點數餘額

---

## 使用者情境與測試

### 使用者故事 1 — 瀏覽商品列表並篩選分類（優先級：P1）

玩家進入商品列表頁，依分類瀏覽感興趣的抽獎商品。

**此優先級的原因**：商品列表是平台的主要探索入口，流量最高的頁面，直接影響轉換率。

**獨立測試**：
1. 進入 `/lottery`，確認商品卡片顯示。
2. 點擊「一番賞」分類 Tab，確認只顯示對應商品。

**驗收情境**：
1. **在** 玩家進入 `/lottery`，**當** `POST /api/lottery/browse/list` 回應，**則** 顯示所有 `status = ON_SHELF` 的商品卡片，預設依「最熱門」排序（`sortField: "hotCount"`, `sortOrder: "DESC"`）。
2. **在** 分類 Tab，**當** 玩家點擊「扭蛋」，**則** 呼叫 API（condition.category = 扭蛋枚舉值），列表更新，URL 更新為 `/lottery?category=GACHA`（支援分享連結）。
3. **在** 商品卡片，**當** 顯示，**則** 包含：封面圖、商品名稱（最多 2 行）、店家名稱、每抽金幣數、剩餘張數與進度條（已抽/總張數）、分類標籤。
4. **在** 商品剩餘張數 ≤ 總張數的 10%，**當** 卡片顯示，**則** 顯示橙色「即將售完」標籤。
5. **在** 商品 `status = COMPLETED`，**當** 卡片顯示，**則** 卡片加上灰色蒙層，顯示「已售完」，不可點擊進入抽獎（但可點擊查看詳情）。
6. **在** 排序選擇器，**當** 玩家選擇「剩餘最少」，**則** 呼叫 API 並傳入 `sortField: "remainingDraws", sortOrder: "ASC"`。

### 使用者故事 2 — 瀏覽商品詳情並查看獎品清單（優先級：P1）

玩家點擊商品卡片，查看詳細資訊與所有獎品等級分佈。

**此優先級的原因**：獎品詳情是玩家決策是否抽獎的關鍵資訊，資訊完整度直接影響抽獎意願。

**驗收情境**：
1. **在** 玩家點擊商品卡片，**當** 導向 `/lottery/{id}`，**則** 呼叫 `GET /api/lottery/browse/{id}` 取得詳情，同時靜默呼叫 `POST /api/lottery/browse/{id}/hot`（increment: 1）。
2. **在** 商品詳情頁，**當** 顯示獎品清單，**則** 依等級分組顯示所有獎品：等級名稱、賞品圖片、賞品名稱、原始數量、剩餘數量（`remaining`）、已抽數量（`drawnCount`）。
3. **在** 商品未開賣（`startTime` 在未來），**當** 詳情頁顯示，**則** 顯示開賣倒數計時器（格式 D天 HH:MM:SS），「立即抽獎」按鈕替換為「即將開賣」（禁用）。
4. **在** 開賣倒數歸零，**當** 時間到達 `startTime`，**則** 按鈕自動切換為「立即抽獎 — XXX 金幣」（啟用），不需重整頁面。
5. **在** 商品 `status = COMPLETED`，**當** 詳情頁顯示，**則** 按鈕顯示「已售完」（禁用），獎品清單仍可查看（歷史記錄）。

### 使用者故事 3 — 未登入玩家點擊抽獎按鈕（優先級：P1）

未登入的訪客查看商品後點擊抽獎，系統引導登入。

**此優先級的原因**：登入牆設計影響轉換率，必須流暢引導訪客完成登入後返回抽獎。

**驗收情境**：
1. **在** 未登入的訪客，**當** 點擊「立即抽獎」按鈕，**則** 顯示「請先登入以繼續抽獎」提示 Modal，包含「前往登入」按鈕。
2. **在** 玩家點擊「前往登入」，**當** 導向 `/login`，**則** URL 附帶 `redirectTo=/lottery/{id}/draw` 參數，登入成功後自動返回抽獎頁。
3. **在** 訪客未登入，**當** 瀏覽商品列表和詳情頁，**則** 頁面完整顯示（不需登入瀏覽），只有點擊「抽獎」才觸發登入牆。

### 使用者故事 4 — 從商品詳情進入抽獎頁（優先級：P1）

已登入玩家在商品詳情頁點擊抽獎按鈕，跳轉至抽獎頁面。

**驗收情境**：
1. **在** 已登入玩家點擊「立即抽獎 — XXX 金幣」，**當** 玩家有足夠金幣，**則** 導向 `/lottery/{id}/draw`，顯示票券格子與抽獎介面。
2. **在** 已登入玩家，**當** 金幣餘額不足以支付一次抽獎費用，**則** 抽獎按鈕顯示「點數不足」（禁用），並在旁顯示「儲值」連結（導向 `/wallet/topup`）。
3. **在** 進入抽獎頁，**當** 商品有保護時間（`session.isOpener = false`，保護期未結束），**則** 顯示保護倒數計時器，抽獎按鈕禁用。

---

## API 串接規格

### 商品瀏覽

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 商品列表 | POST | `/api/lottery/browse/list` | `condition.category`, `condition.storeId`, `condition.keyword`, `condition.status`, `page`, `size`, `sortField`, `sortOrder` | `id`, `storeId`, `storeName`, `title`, `imageUrl`, `category`, `categoryName`, `playMode`, `pricePerDraw`, `currentPrice`, `maxDraws`, `remainingDraws`, `status`, `startTime` | 商品卡片格子 |
| 商品詳情 | GET | `/api/lottery/browse/{id}` | 無 | `id`, `storeId`, `storeName`, `title`, `description`, `imageUrl`, `category`, `playMode`, `pricePerDraw`, `currentPrice`, `maxDraws`, `totalDraws`, `remainingDraws`, `status`, `prizes[]`, `startTime` | 商品詳情頁 |
| 增加熱門計數 | POST | `/api/lottery/browse/{lotteryId}/hot` | `increment: 1` | 無（靜默） | 無 UI 狀態 |
| 取得票券與 Session | GET | `/api/lottery/draw/{lotteryId}/tickets` | 無 | `tickets[]`, `session` | 抽獎頁初始化 |
| 執行抽獎 | POST | `/api/lottery/draw/{lotteryId}/draw` | `count`, `ticket`(optional) | 獎品陣列 | 動畫 → Modal |

#### 商品列表 Request 範例
```json
POST /api/lottery/browse/list
{
  "condition": {
    "category": "ICHIBAN",
    "status": "ON_SHELF",
    "keyword": ""
  },
  "page": 1,
  "size": 20,
  "sortField": "hotCount",
  "sortOrder": "DESC"
}
```

#### 商品詳情獎品 Response 範例
```json
"prizes": [
  {
    "id": "prize-uuid",
    "level": "A",
    "levelName": "A賞",
    "name": "鋼彈 RX-78-2 模型",
    "imageUrl": "https://cdn.example.com/prizes/a-prize.jpg",
    "quantity": 1,
    "remaining": 1,
    "drawnCount": 0,
    "prizeType": "PHYSICAL",
    "pointValue": 0
  }
]
```

---

## 功能需求（前端 UI）

### 商品卡片

- **FR-UI-001**：商品卡片在桌機為 4 欄、平板為 2 欄、手機為 2 欄（較小間距）。
- **FR-UI-002**：剩餘張數進度條：`remainingDraws / maxDraws * 100%`，綠色（> 50%）/ 黃色（10–50%）/ 紅色（< 10%）。
- **FR-UI-003**：封面圖使用 `object-fit: cover`，比例 3:4（直式），圖片載入前顯示骨架屏。
- **FR-UI-004**：遊戲模式標籤（一番賞 / 扭蛋 / 刮刮樂 / 卡牌）以小標籤顯示，顏色依分類區分。
- **FR-UI-005**：無限捲動：當用戶捲動到列表底部 200px 前，觸發下一頁 API 請求（`page + 1`）。

### 商品詳情頁

- **FR-UI-006**：開賣倒數計時器使用精確到秒的 `setInterval` 更新，格式「X天 HH時MM分SS秒後開賣」。
- **FR-UI-007**：獎品清單依等級排序（S > A > B > C > 謝謝惠顧），每個等級可折疊展開。
- **FR-UI-008**：`prizes[].remaining = 0` 的獎品顯示刪除線與「已抽完」標記。
- **FR-UI-009**：商品 `description` 富文本使用安全 HTML 渲染（DOMPurify）。
- **FR-UI-010**：「立即抽獎」按鈕固定在底部（Sticky Bottom Bar）在手機版，桌機版在商品資訊卡旁側。

### URL 狀態管理（Query String）

- **FR-UI-011**：分類選擇同步至 URL query（`/lottery?category=GACHA`），支援分享與瀏覽器返回。
- **FR-UI-012**：搜尋關鍵字同步至 URL query（`/lottery?keyword=鋼彈`），使用 debounce 500ms 更新 URL 與 API。

### 狀態管理

- **SM-001**：`products` — 商品陣列（含分頁累積，無限捲動追加）。
- **SM-002**：`activeCategory` — 當前分類篩選值。
- **SM-003**：`sortConfig` — `{ field, order }` 排序設定。
- **SM-004**：`searchKeyword` — debounce 搜尋關鍵字。
- **SM-005**：`currentPage` / `hasMore` — 分頁狀態。
- **SM-006**：`productDetail` — 詳情頁商品物件（含 prizes 陣列）。
- **SM-007**：`countdownTimer` — 開賣倒數計時器 ID。

---

## API 驗證清單

- **AV-001** ✅：`POST /api/lottery/browse/list` 支援 `category`、`keyword`、`sortField`、`sortOrder` 參數 — 已確認。
- **AV-002** ✅：`GET /api/lottery/browse/{id}` 返回 `prizes[]` 完整獎品池資訊 — 已確認。
- **AV-003** ✅：商品 `status` 枚舉：`DRAFT`（不顯示）/ `ON_SHELF`（可玩）/ `OFF_SHELF`（不顯示）/ `COMPLETED`（顯示但不可抽）— 已確認。
- **AV-004** ⚠️：`category` 的枚舉值（如 `ICHIBAN`, `GACHA`, `SCRATCH`, `CARD`）在 API 中的實際名稱需與後端 spec 確認，避免前端傳錯值。
- **AV-005** ⚠️：`playMode` 欄位（`LOTTERY_MODE` / `SCRATCH_STORE` / `SCRATCH_PLAYER`）在商品列表 Response 中是否存在？前端需此值在列表頁顯示遊戲模式標籤。
- **AV-006** ❌：`POST /api/lottery/browse/list` 的 Response 缺少分頁 metadata（`totalElements`, `hasMore`）— 無限捲動需要知道是否還有下一頁，建議後端補充。
- **AV-007** ⚠️：`currentPrice` vs `pricePerDraw` 兩個欄位的差異？是否 `currentPrice` 是折扣後價格、`pricePerDraw` 是原價？需後端說明。
- **AV-008** ⚠️：`priceType`（金幣 vs 獎勵幣）是否在 API 中返回？某些商品可能只能用金幣或只能用獎勵幣抽，前端需知道以決定按鈕文字。
- **AV-009** ❌：`GET /api/lottery/browse/{id}` 的 `prizes[].prizeType` 包含 `"THANKS"` 或 `null` 以表示謝謝惠顧？需確認如何識別謝謝惠顧獎品。
- **AV-010** ⚠️：`hot` API 是否有防刷機制（同用戶同商品 24 小時內只計一次）？前端無需處理，僅確認後端有此保護。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 成功標準

- **SC-001**：商品列表首屏（20 筆卡片）渲染 ≤ 1.5 秒（含 API 回應）。
- **SC-002**：分類 Tab 切換後列表更新 ≤ 800ms（API 回應 + 渲染）。
- **SC-003**：商品詳情頁封面大圖的 LCP ≤ 2.5 秒（3G 網路）。
- **SC-004**：開賣倒數計時器精準到秒，無誤差累積。
- **SC-005**：無限捲動觸發時機準確（到底部前 200px），不出現重複載入或跳頁。
- **SC-006**：「即將售完」標籤計算正確（`remainingDraws / maxDraws ≤ 0.1`），不出現錯誤標示。
- **SC-007**：商品詳情頁「hot」API 的靜默呼叫不阻塞頁面渲染（Fire-and-forget）。
