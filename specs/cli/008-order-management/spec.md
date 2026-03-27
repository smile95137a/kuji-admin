# 前端規格書（前台）：訂單管理

**功能分支**：`cli/008-order-management`
**對應後端 Spec**：`specs/008-order-management/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：需登入（ROLE_USER）

---

## 頁面與介面清單

### 頁面 1 — 訂單列表頁
- **路由**：`/orders`
- **存取權限**：需登入
- **UI 元件**：
  - 訂單狀態篩選 Tabs（全部 / 待出貨 / 準備中 / 已出貨 / 已完成）
  - 訂單卡片列表（Order Card List）
  - 每張卡片：訂單號、店家名稱、賞品數量、出貨方式、建立時間、狀態 Badge
  - 空狀態插圖（無訂單時）
  - 捲動至頂部按鈕（ScrollToTop）

### 頁面 2 — 訂單詳情頁
- **路由**：`/orders/{id}`
- **存取權限**：需登入
- **UI 元件**：
  - 訂單基本資訊卡（訂單號、建立時間、店家名稱）
  - 訂單狀態 Badge（大型）
  - 狀態歷史時間軸（Status History Timeline）
  - 賞品清單（Prize List with 圖片 + 等級 + 名稱 + 估計價值）
  - 出貨方式標籤（宅配到府 / 超商取貨）
  - 收件資訊卡（唯讀：姓名、電話、地址）
  - 返回訂單列表按鈕
  - 相關商品連結（回到商品頁的快捷入口）

---

## 使用者情境與測試

### 使用者故事 1 — 瀏覽個人訂單列表（優先級：P1）

玩家進入訂單列表頁，查看所有出貨訂單的概覽資訊。

**此優先級的原因**：訂單列表是玩家管理出貨需求的主要入口，出貨後玩家最頻繁查看的頁面。

**獨立測試**：
1. 以有訂單記錄的玩家進入 `/orders`。
2. 確認所有訂單以卡片形式列出，依建立時間降序排列。

**驗收情境**：
1. **在** 玩家進入 `/orders`，**當** `GET /api/orders` 回應，**則** 顯示所有訂單卡片，依 `createdAt` 降序排列（最新在頂部）。
2. **在** 訂單列表，**當** 玩家點擊「已出貨」Tab，**則** 客戶端過濾只顯示 `status = SHIPPED` 的訂單卡片（不重新呼叫 API）。
3. **在** 訂單卡片，**當** 顯示，**則** 可見：訂單號（orderNo）、店家名稱（storeName）、賞品數量（prizeCount 件）、出貨方式（shippingMethodName）、建立日期、狀態 Badge。
4. **在** 玩家沒有任何訂單，**當** API 返回空陣列，**則** 顯示空狀態插圖（包裹圖示）與文字「尚無訂單記錄」，附帶「前往商品頁」CTA 按鈕（導向 `/lottery`）。
5. **在** 訂單卡片，**當** 玩家點擊任一卡片，**則** 導向 `/orders/{id}` 訂單詳情頁。

### 使用者故事 2 — 查看訂單詳情與出貨狀態（優先級：P1）

玩家在訂單詳情頁查看完整出貨資訊與狀態進度。

**此優先級的原因**：玩家在等待包裹期間，訂單詳情頁是主要的查詢管道。

**獨立測試**：
1. 點擊任一訂單卡片進入詳情頁。
2. 確認 `GET /api/orders/{id}` 回傳的所有欄位均正確顯示。

**驗收情境**：
1. **在** 訂單詳情頁載入，**當** `GET /api/orders/{id}` 回應，**則** 顯示：訂單號、狀態（大型 Badge）、店家名稱、賞品列表（圖片 + 名稱 + 等級）、出貨方式、收件資訊。
2. **在** 狀態歷史時間軸，**當** `statusHistory` 包含多個節點，**則** 最新狀態在頂部，每個節點顯示狀態名稱與時間戳（YYYY/MM/DD HH:mm）。
3. **在** 訂單狀態為「已出貨（SHIPPED）」，**當** 頁面顯示，**則** 時間軸的「已出貨」節點高亮（active 樣式），後續節點（如「已完成」）以灰色顯示（待完成）。
4. **在** 收件資訊區塊，**當** 顯示，**則** 以唯讀樣式呈現（不顯示編輯圖示，也不顯示編輯按鈕），並標示「此資訊為出貨時確認，不可更改」。
5. **在** 賞品列表，**當** 某賞品的 `prizeImageUrl` 為空，**則** 顯示預設賞品圖示（不顯示破圖）。

### 使用者故事 3 — 不同出貨方式的收件資訊顯示（優先級：P2）

宅配與超商取貨的收件資訊格式不同，需分別處理顯示邏輯。

**驗收情境**：
1. **在** 訂單為「宅配到府（HOME_DELIVERY）」，**當** 顯示收件資訊，**則** 顯示：收件人姓名、電話、收件地址（三行格式）。
2. **在** 訂單為「超商取貨（CONVENIENCE_STORE）」，**當** 顯示收件資訊，**則** 顯示：收件人姓名、電話、超商名稱、超商店碼、超商地址。
3. **在** 訂單詳情頁，**當** 出貨方式顯示，**則** 使用對應圖示（🏠 宅配到府 / 🏪 超商取貨）幫助玩家快速識別。

### 使用者故事 4 — 訂單完成後跳轉至商品頁（優先級：P3）

玩家在訂單詳情頁可快速跳回對應商品頁，激發再次購買意願。

**驗收情境**：
1. **在** 訂單詳情頁，**當** 頁面顯示，**則** 顯示「再次前往此商品」按鈕，導向 `/lottery/{lotteryId}`（若 `lotteryId` 存在於訂單資料）。
2. **在** 訂單詳情頁底部，**當** 顯示推薦商品，**則** 依店家 ID 列出 2–3 個同店家的熱門商品（若有此資料）。

---

## API 串接規格

### 訂單管理

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得訂單列表 | GET | `/api/orders` | 無 | `id`, `orderNo`, `status`, `statusName`, `storeName`, `prizeCount`, `shippingMethod`, `shippingMethodName`, `totalValue`, `createdAt` | 訂單卡片列表 |
| 取得訂單詳情 | GET | `/api/orders/{id}` | 無 | `id`, `orderNo`, `status`, `storeName`, `prizes[{name, level, imageUrl, prizeValue}]`, `shippingMethod`, `shippingInfo.recipientName`, `shippingInfo.recipientPhone`, `shippingInfo.address`, `statusHistory[{status, timestamp}]`, `createdAt` | 詳情頁渲染 |

#### 訂單詳情 Response 範例
```json
{
  "id": "order-uuid-1",
  "orderNo": "ORD-20260320-001",
  "status": "SHIPPED",
  "storeName": "一番賞天堂",
  "prizes": [
    {
      "name": "A賞 鋼彈 RX-78-2",
      "level": "A",
      "imageUrl": "https://cdn.example.com/prizes/a-prize.jpg",
      "prizeValue": 1500
    }
  ],
  "shippingMethod": "HOME_DELIVERY",
  "shippingInfo": {
    "recipientName": "王小明",
    "recipientPhone": "0912345678",
    "address": "台北市信義區信義路五段7號"
  },
  "statusHistory": [
    { "status": "PENDING", "timestamp": "2026-03-20T10:00:00" },
    { "status": "PREPARING", "timestamp": "2026-03-21T09:30:00" },
    { "status": "SHIPPED", "timestamp": "2026-03-22T14:00:00" }
  ],
  "createdAt": "2026-03-20T10:00:00"
}
```

---

## 功能需求（前端 UI）

### 訂單列表頁

- **FR-UI-001**：狀態 Tab 數量顯示在 Tab 標籤旁（如「待出貨 (3)」）。
- **FR-UI-002**：訂單卡片右側顯示狀態 Badge（顏色定義見下表）。
- **FR-UI-003**：訂單卡片為可點擊的整塊區域（cursor: pointer），點擊任何位置均導向詳情。
- **FR-UI-004**：訂單列表使用客戶端過濾（不重新呼叫 API），切換 Tab 響應 ≤ 50ms。

### 訂單狀態 Badge 顏色規範

| 狀態 | 英文值 | Badge 顏色 | 時間軸圖示 |
|------|--------|-----------|-----------|
| 待出貨 | PENDING | 藍色 | ⏳ |
| 準備中 | PREPARING | 黃色 | 📦 |
| 已出貨 | SHIPPED | 橙色 | 🚚 |
| 已完成 | COMPLETED | 綠色 | ✅ |

### 狀態時間軸

- **FR-UI-005**：時間軸垂直排列，已完成的步驟顯示填色圓點，當前步驟顯示脈衝動畫圓點，未來步驟顯示空心灰色圓點。
- **FR-UI-006**：時間軸固定顯示所有可能的狀態節點（PENDING → PREPARING → SHIPPED → COMPLETED），即使某些狀態尚未發生也顯示（以灰色表示未到達）。
- **FR-UI-007**：每個已完成節點顯示實際完成時間（`timestamp`），未到達節點不顯示時間。

### 賞品清單

- **FR-UI-008**：賞品等級顏色代碼（與賞品盒頁一致）：S/SP — 金色、A — 紅色、B — 橙色、C — 綠色。
- **FR-UI-009**：`prizeValue`（估計價值）以淡色小字顯示（「參考價值：NT$ XXX」），非主要資訊。

### 狀態管理

- **SM-001**：`orders` — 從 `GET /api/orders` 取得的全部訂單陣列，一次性載入。
- **SM-002**：`activeStatusFilter` — 當前篩選狀態（`ALL` | `PENDING` | `PREPARING` | `SHIPPED` | `COMPLETED`）。
- **SM-003**：`filteredOrders` — 計算屬性（computed），依 `activeStatusFilter` 從 `orders` 中過濾。
- **SM-004**：`orderDetail` — 訂單詳情頁的當前訂單物件。

---

## API 驗證清單

- **AV-001** ✅：`GET /api/orders` 回傳 `statusName`（中文狀態名稱）供 Tab 和 Badge 顯示 — 確認後端提供。
- **AV-002** ✅：`GET /api/orders/{id}` 回傳 `statusHistory` 陣列供時間軸渲染 — 已確認。
- **AV-003** ✅：`shippingInfo` 包含 `recipientName`、`recipientPhone`、`address` 供顯示收件資訊 — 已確認。
- **AV-004** ⚠️：`shippingInfo.address` 在超商取貨時是否包含超商店碼（`storeCode`）和超商名稱（`storeName`）？需後端確認 `shippingInfo` 結構在宅配/超商模式下的差異。
- **AV-005** ⚠️：`GET /api/orders` 是否一次返回所有訂單（無分頁）？若玩家有大量訂單（>100筆），效能和 UX 可能受影響。
- **AV-006** ❌：訂單詳情 Response 缺少 `lotteryId` 欄位 — 無法從訂單頁導向原始商品頁（「再次前往此商品」功能）。
- **AV-007** ❌：訂單詳情 Response 缺少快遞單號（`trackingNumber`）和快遞公司（`carrierName`） — 狀態為「已出貨」時，玩家無法自行追蹤包裹，需後端補充。
- **AV-008** ⚠️：`prizes[].level` 的枚舉值定義（S、A、B、C 等）是否與後端 spec 一致？顏色代碼 mapping 需雙方確認。
- **AV-009** ⚠️：`statusHistory` 中的 `status` 是英文枚舉（如 `SHIPPED`）還是中文名稱？時間軸顯示需中文，建議後端返回或前端自行 mapping。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 成功標準

- **SC-001**：訂單列表在 50 筆訂單下首次渲染 ≤ 1 秒（含 API 回應時間）。
- **SC-002**：狀態 Tab 切換（客戶端過濾）響應 ≤ 50ms。
- **SC-003**：訂單詳情頁在 API 載入完成後 ≤ 300ms 內完整渲染。
- **SC-004**：狀態時間軸當前步驟的脈衝動畫不影響頁面其他元素（不產生 Layout Shift）。
- **SC-005**：空狀態頁面中的「前往商品頁」CTA 按鈕可點擊，成功導向 `/lottery`。
- **SC-006**：訂單詳情中的收件資訊欄位不出現任何可編輯的 input/textarea 元素（純展示模式）。
