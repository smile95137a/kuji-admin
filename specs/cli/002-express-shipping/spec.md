# 前端規格書（前台）：物流出貨

**功能分支**：`cli/002-express-shipping`
**對應後端 Spec**：`specs/002-express-shipping/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：需登入（ROLE_USER）

---

## 頁面與介面清單

### 頁面 1 — 賞品盒出貨確認頁（Shipment Checkout Modal / Page）
- **路由**：`/prize-box/ship`（或從賞品盒頁以 Modal 形式呈現）
- **存取權限**：需登入
- **UI 元件**：
  - 已選賞品摘要卡（Selected Prize Summary Cards）
  - 出貨方式選擇器（宅配到府 / 超商取貨）
  - 收件人姓名輸入框
  - 收件人電話輸入框
  - 收件地址輸入框（宅配模式）
  - 超商店碼 / 店名 / 地址輸入框（超商模式）
  - 已儲存地址下拉選單（Pre-fill from Saved Addresses）
  - 提交按鈕（確認出貨）
  - 表單驗證錯誤訊息
  - 多店家拆單警告橫幅

### 頁面 2 — 訂單詳情頁（出貨狀態追蹤）
- **路由**：`/orders/{id}`
- **存取權限**：需登入
- **UI 元件**：
  - 訂單狀態時間軸（Status History Timeline）
  - 收件資訊卡（Recipient Info Card）
  - 賞品列表
  - 出貨方式標籤
  - 返回按鈕

---

## 使用者情境與測試

### 使用者故事 1 — 選擇宅配出貨（優先級：P1）

玩家在賞品盒選取賞品後，填寫宅配資訊並提交，系統建立訂單。

**此優先級的原因**：宅配是最主要的出貨方式，是玩家獲得實體賞品的關鍵流程。

**獨立測試**：
1. 準備至少 1 筆 `status=IN_BOX` 且 `isShippable=true` 的賞品。
2. 進入賞品盒頁選取賞品，點擊「申請出貨」。
3. 選擇「宅配到府」，填入資料後確認。

**驗收情境**：
1. **在** 出貨表單顯示，**當** 玩家選擇「宅配到府」，**則** 顯示收件人姓名、電話、地址三個必填欄位，超商相關欄位隱藏。
2. **在** 玩家有已儲存地址，**當** 表單開啟，**則** 自動帶入預設地址資料至各欄位。
3. **在** 表單已填寫完畢，**當** 玩家點擊「確認出貨」，**則** 呼叫 `POST /api/prize-box/ship`，顯示載入中狀態，按鈕禁用防止重複提交。
4. **在** API 回應成功，**當** 訂單建立完成，**則** 關閉 Modal，顯示成功通知（Toast），並導向 `/orders/{orderId}`。
5. **在** API 回應失敗，**當** 提交失敗，**則** 顯示錯誤訊息（如「庫存不足，請重新選擇」），不關閉表單。

### 使用者故事 2 — 選擇超商取貨（優先級：P1）

玩家選擇超商取貨方式，填入超商資訊完成出貨。

**此優先級的原因**：超商取貨是台灣常見配送方式，必須完整支援。

**獨立測試**：選擇出貨方式後切換至「超商取貨」，確認欄位切換。

**驗收情境**：
1. **在** 玩家點擊「超商取貨」，**當** 選項切換，**則** 顯示超商店碼、店名、超商地址三個欄位，宅配地址欄位隱藏。
2. **在** 超商欄位已填寫，**當** 玩家提交，**則** Request body 中 `shippingMethod` 為 `"CONVENIENCE_STORE"`，`storeCode`、`storeName`、`storeAddress` 正確帶入。
3. **在** 超商店碼欄位，**當** 玩家輸入空白或格式錯誤，**則** 顯示「請輸入有效的超商店碼」驗證錯誤。

### 使用者故事 3 — 多店家拆單警告（優先級：P2）

玩家選取來自不同店家的賞品同時申請出貨，系統警告將拆成多筆訂單。

**此優先級的原因**：多店家拆單是隱性行為，玩家不知情會產生混淆。

**獨立測試**：選取 2 個不同 `storeId` 的賞品，進入出貨頁面。

**驗收情境**：
1. **在** 出貨表單開啟，**當** 已選賞品來自 2 個以上不同店家，**則** 表單頂部顯示警告橫幅：「您選取的賞品來自 N 個店家，將自動拆分為 N 筆出貨訂單」。
2. **在** 拆單成功，**當** API 回應包含多筆 orders，**則** 成功頁面列出所有訂單號，每筆均可點擊查看。

### 使用者故事 4 — 查看出貨狀態（優先級：P2）

玩家在訂單詳情頁追蹤出貨進度。

**驗收情境**：
1. **在** 訂單詳情頁，**當** 頁面載入，**則** 狀態時間軸顯示 `statusHistory` 中所有狀態節點，最新狀態在頂部。
2. **在** 訂單狀態為 `PENDING`，**當** 頁面顯示，**則** 出貨方式、收件資訊以唯讀模式呈現，不顯示修改按鈕。

---

## API 串接規格

### 出貨相關

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 建立出貨訂單 | POST | `/api/prize-box/ship` | `prizeBoxIds`, `shippingMethod`, `recipientName`, `recipientPhone`, `recipientAddress` / `storeCode`, `storeName`, `storeAddress` | `orderId`, `orderNo`, `status`, `prizes`, `shippingInfo` | 提交中 → 成功/失敗 Toast |
| 取得已儲存地址 | GET | `/api/address` | 無 | `id`, `recipientName`, `recipientPhone`, `address`, `isDefault`, `addressType` | 表單載入時預填 |
| 取得訂單詳情 | GET | `/api/orders/{id}` | 無 | `id`, `orderNo`, `status`, `prizes`, `shippingMethod`, `recipientInfo`, `statusHistory` | 顯示狀態時間軸 |

#### 宅配 Request 範例
```json
POST /api/prize-box/ship
{
  "prizeBoxIds": ["box-uuid-1", "box-uuid-2"],
  "shippingMethod": "HOME_DELIVERY",
  "recipientName": "王小明",
  "recipientPhone": "0912345678",
  "recipientAddress": "台北市信義區信義路五段7號"
}
```

#### 超商 Request 範例
```json
POST /api/prize-box/ship
{
  "prizeBoxIds": ["box-uuid-3"],
  "shippingMethod": "CONVENIENCE_STORE",
  "storeCode": "123456",
  "storeName": "7-Eleven 信義店",
  "storeAddress": "台北市信義區松高路1號"
}
```

---

## 功能需求（前端 UI）

### 介面需求

- **FR-UI-001**：出貨方式以 Radio Button 或分頁 Tab 實現，切換時動態顯示/隱藏對應欄位（動畫過渡，不閃爍）。
- **FR-UI-002**：已儲存地址下拉選單中，預設地址排在第一位並標示「預設」標籤；選擇後自動填入收件人姓名、電話、地址。
- **FR-UI-003**：「確認出貨」按鈕在表單提交期間顯示 Spinner 並禁用，防止重複點擊。
- **FR-UI-004**：成功建立訂單後，自動跳轉至 `/orders/{orderId}` 並顯示 Toast「出貨申請成功！」。
- **FR-UI-005**：多店家警告使用 `yellow` 警告橫幅，不阻止玩家繼續提交。
- **FR-UI-006**：手機版表單使用全螢幕頁面（非 Modal），桌機版以 Modal 呈現，最大寬度 600px。
- **FR-UI-007**：電話欄位使用 `type="tel"` 輸入框，自動觸發手機數字鍵盤。
- **FR-UI-008**：表單所有欄位都需即時驗證（onBlur），不需等到提交時才顯示錯誤。

### 表單驗證規則

| 欄位 | 驗證規則 | 錯誤訊息 |
|------|---------|---------|
| 收件人姓名 | 必填，2–20 字元 | 「請輸入收件人姓名（2–20 字）」 |
| 收件人電話 | 必填，台灣手機格式 `09XXXXXXXX` | 「請輸入有效的手機號碼」 |
| 收件地址（宅配）| 必填，10 字元以上 | 「請輸入完整地址」 |
| 超商店碼 | 必填，6 位數字 | 「請輸入有效的超商店碼（6位數）」 |
| 超商店名 | 必填，2–50 字元 | 「請輸入超商店名」 |

### 狀態管理

- **SM-001**：`shippingMethod` — `"HOME_DELIVERY"` | `"CONVENIENCE_STORE"`，預設 `"HOME_DELIVERY"`。
- **SM-002**：`savedAddresses` — 從 GET /api/address 取得的地址陣列。
- **SM-003**：`isSubmitting` — 控制按鈕禁用狀態。
- **SM-004**：`submitError` — API 錯誤訊息字串，顯示在表單底部。
- **SM-005**：`selectedPrizeIds` — 從賞品盒頁傳入的已選賞品 ID 陣列（透過路由 state 或 store）。

---

## API 驗證清單

- **AV-001** ✅：`POST /api/prize-box/ship` 支援 `HOME_DELIVERY` 和 `CONVENIENCE_STORE` 兩種 `shippingMethod` — 已確認。
- **AV-002** ✅：`GET /api/address` 回傳 `isDefault` 欄位，前端可識別預設地址 — 已確認。
- **AV-003** ✅：`GET /api/orders/{id}` 回傳 `statusHistory` 陣列供時間軸顯示 — 已確認。
- **AV-004** ⚠️：`POST /api/prize-box/ship` 的 Response 在多店家拆單時是否回傳 `orders` 陣列？規格顯示只回傳單筆 `orderId` — 需確認拆單時 Response 格式。
- **AV-005** ⚠️：`GET /api/address` 的 `addressType` 欄位用途不明確 — 是區分宅配/超商地址？需後端確認語意。
- **AV-006** ❌：沒有「取得預設地址」的獨立 API（`GET /api/address/default`）被用於 010-prize-box 規格中，但此規格的 API 清單未列出 — 需確認是否存在此端點。
- **AV-007** ⚠️：`shippingInfo` 在 `GET /api/orders/{id}` Response 中的欄位結構（`recipientInfo` vs `shippingInfo`）名稱需統一。
- **AV-008** ❌：Response 中缺少快遞單號（`trackingNumber`）欄位 — 訂單「已出貨」後玩家無法追蹤包裹，建議後端補充。
- **AV-009** ⚠️：是否有訂單取消 API？規格說明玩家不可取消（只讀），但待確認是否在特定狀態允許取消。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 成功標準

- **SC-001**：出貨表單提交後，訂單建立 API 回應時間 ≤ 3 秒（P95）。
- **SC-002**：表單驗證錯誤在欄位離焦（onBlur）後 100ms 內顯示。
- **SC-003**：地址預填在下拉選單選擇後 50ms 內完成填入。
- **SC-004**：多店家警告在選取第二個不同店家賞品後立即顯示。
- **SC-005**：宅配與超商欄位切換動畫時長 ≤ 300ms，不產生版面跳動（Layout Shift）。
- **SC-006**：已選賞品摘要在表單頂部清晰展示（賞品圖、名稱、等級），讓玩家確認選擇無誤。
