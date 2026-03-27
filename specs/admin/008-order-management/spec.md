# 前端規格書（後台）：訂單管理

**功能分支**：`008-order-management`
**對應後端 Spec**：`specs/008-order-management/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：後台管理介面（Admin Panel）
**存取角色**：ROLE_ADMIN、ROLE_STORE_OWNER

---

## 頁面與介面清單

### 頁面 1 — 訂單列表頁
- **路由**：`/admin/orders`
- **存取權限**：ROLE_ADMIN（跨店）、ROLE_STORE_OWNER（限自家店）
- **UI 元件**：
  - 頁面標題「訂單管理」
  - 篩選列：
    - 店家下拉（ROLE_ADMIN 可見，含「全部店家」選項；ROLE_STORE_OWNER 隱藏）
    - 訂單狀態下拉（全部 / 待出貨 / 準備中 / 已出貨 / 已完成 / 已取消）
    - 關鍵字搜尋（訂單編號、玩家 Email）
    - 日期範圍（建立時間）
    - 排序下拉（建立時間 DESC/ASC、更新時間 DESC/ASC）
  - 「重新整理」按鈕
  - 資料表格：
    - 欄位：訂單編號、店家名稱（Admin 限）、玩家 Email、配送方式、獎品數量、訂單金額、狀態徽章、建立時間、最後更新時間、操作
    - 操作：查看詳情、更新狀態（下一步）、取消
  - 分頁（每頁 20 筆）

### 頁面 2 — 訂單詳情頁
- **路由**：`/admin/orders/{id}`
- **存取權限**：ROLE_ADMIN、ROLE_STORE_OWNER
- **UI 元件**：
  - 返回按鈕 + 麵包屑（訂單管理 > 訂單 #{orderNo}）
  - 訂單狀態徽章（大型，頁面頂部）
  - 基本資訊卡片：訂單編號、建立時間、最後更新
  - 店家資訊區塊：店家名稱（可點擊跳轉）
  - 玩家資訊區塊：Email、暱稱
  - 配送資訊區塊（依 shippingMethod 動態顯示）：
    - 超商取貨：超商類型、門市名稱、門市代碼、地址
    - 宅配：收件人、電話、地址
  - 獎品列表（卡片格式：獎品圖片、名稱、等級徽章）
  - 狀態歷程時間軸（垂直，最新在上）：狀態名稱、操作員 Email、時間戳記
  - 操作區域（右側固定 sidebar 或底部 action bar）：
    - 狀態前進按鈕（依當前狀態動態生成）
    - 取消訂單按鈕（僅特定狀態可用）

### 頁面 3 — 更新狀態確認 Modal
- **路由**：`/admin/orders/{id}`（Modal 覆蓋）
- **存取權限**：ROLE_ADMIN、ROLE_STORE_OWNER
- **UI 元件**：
  - Modal 標題「確認更新狀態」
  - 說明文字：「確定將訂單 #{orderNo} 從「{currentStatus}」更新為「{nextStatus}」？」
  - 確認 / 取消按鈕

### 頁面 4 — 取消訂單 Modal
- **路由**：`/admin/orders/{id}`（Modal 覆蓋）
- **存取權限**：ROLE_ADMIN、ROLE_STORE_OWNER
- **UI 元件**：
  - Modal 標題「取消訂單」
  - 警告說明：「取消後，訂單中的 {count} 件獎品將自動退回玩家的獎品箱。」
  - 取消原因輸入（必填，最多 200 字）
  - 確認取消 / 返回按鈕

---

## 使用者情境與測試

### 使用者故事 1 — Admin 跨店查詢並審查特定訂單（優先級：P1）

Super Admin 需要查看所有店家的訂單，並針對特定訂單進行稽核或協助客服處理問題。

**此優先級的原因**：跨店查詢是 Admin 角色的核心稽核功能。

**獨立測試**：Admin 帳號登入，確認訂單列表顯示多個店家的訂單，並可透過店家篩選縮小範圍。

**驗收情境**：
1. **在** Admin 登入後進入訂單管理，**當** 列表載入，**則** 顯示所有店家的訂單，包含「店家名稱」欄位。
2. **在** Admin 在篩選列選擇特定店家，**當** 查詢，**則** 列表僅顯示該店家的訂單。
3. **在** Admin 進入訂單詳情，**當** 查看狀態歷程，**則** 顯示每次狀態變更的操作員 Email 與時間戳記。

---

### 使用者故事 2 — 店家管理員管理自家訂單狀態（優先級：P1）

店家管理員只能看到和操作自家的訂單，依序推進狀態直到完成。

**此優先級的原因**：訂單狀態管理是店家日常核心操作。

**獨立測試**：StoreOwner 帳號登入，確認列表只有自家訂單，並完成一筆訂單從「待出貨」到「已完成」的完整流程。

**驗收情境**：
1. **在** StoreOwner 登入，**當** 進入訂單管理，**則** 不顯示其他店家訂單，且無店家篩選下拉。
2. **在** 訂單狀態為「準備中」，**當** 點擊「標記已出貨」並確認，**則** 狀態更新為「已出貨」，歷程記錄當前管理員操作。
3. **在** 訂單狀態為「已出貨」，**當** 管理員點擊「確認完成」，**則** 狀態最終變為「已完成」，不可再修改。

---

### 使用者故事 3 — 管理員取消訂單並記錄原因（優先級：P1）

異常訂單（缺貨、玩家要求）需要取消，並記錄原因以備查詢。

**驗收情境**：
1. **在** 訂單狀態為「待出貨」，**當** 管理員輸入取消原因「庫存不足，客服已聯繫玩家」並確認，**則** 訂單狀態變為「已取消」，原因記錄在狀態歷程中。
2. **在** 訂單狀態為「已完成」，**當** 管理員查看詳情，**則** 取消按鈕不存在或為 disabled，tooltip「已完成訂單無法取消」。

---

### 使用者故事 4 — 管理員使用多條件搜尋定位訂單（優先級：P2）

在大量訂單中，管理員需要組合篩選條件快速定位特定訂單。

**驗收情境**：
1. **在** 篩選列，**當** 同時選擇「狀態：待出貨」和「日期：今天」，**則** 只顯示今天建立的待出貨訂單。
2. **在** 關鍵字搜尋欄輸入訂單編號，**當** 搜尋，**則** 精確顯示對應訂單（若存在）。

---

### 邊界情況

- 訂單含 0 件獎品（異常情況），顯示警告樣式「此訂單無獎品資料，請聯繫技術支援」。
- StoreOwner 嘗試直接訪問其他店家訂單的 URL，後端返回 403，前端顯示「無權限存取此訂單」頁面。
- 大量訂單（>1000 筆）時，分頁需正常運作。
- 篩選後無結果，顯示空狀態「沒有符合條件的訂單」。

---

## API 串接規格

### 訂單管理

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 查詢訂單列表 | POST | `/api/admin/orders/list` | `condition:{storeId, status, keyword, startDate, endDate}, sortBy, sortOrder` | `[{id, orderNo, status, storeName, playerEmail, shippingMethod, prizeCount, totalValue, createdAt, statusUpdatedAt}]` | loading → 表格渲染 |
| 取得訂單詳情 | GET | `/api/admin/orders/{id}` | — | `{id, orderNo, status, store, player, prizes, shippingInfo, statusHistory}` | loading spinner → 詳情渲染 |
| 更新訂單狀態 | PUT | `/api/admin/orders/{id}/status` | `status: "PREPARING"\|"SHIPPED"\|"COMPLETED"` | `{id, status, statusUpdatedAt}` | Modal loading → 狀態即時更新 |
| 取消訂單 | POST | `/api/admin/orders/{id}/cancel` | `reason` | `{id, status: "CANCELLED"}` | Modal loading → 關閉 → 狀態更新 |
| 取得店家選單 | GET | `/api/admin/stores/options` | — | `[{id, name}]` | 篩選列下拉載入 |

---

## 功能需求（前端 UI）

### 介面需求

- **FR-UI-001**：訂單狀態徽章顏色語意：
  - 待出貨 → 黃色
  - 準備中 → 藍色
  - 已出貨 → 紫色
  - 已完成 → 綠色
  - 已取消 → 灰色
- **FR-UI-002**：狀態前進按鈕（Action Button）依狀態動態顯示：
  - 待出貨 → 「開始準備」（藍色）
  - 準備中 → 「標記已出貨」（紫色）
  - 已出貨 → 「確認完成」（綠色）
  - 已完成 / 已取消 → 無操作按鈕
- **FR-UI-003**：取消按鈕僅在「待出貨」、「準備中」狀態顯示；其他狀態隱藏。
- **FR-UI-004**：狀態歷程時間軸（Vertical Timeline）：每個節點顯示狀態中文名稱、操作員 Email、格式化時間（YYYY-MM-DD HH:mm:ss）。
- **FR-UI-005**：獎品列表在詳情頁以卡片網格（每行 3-4 張）顯示，含等級徽章（S/A/B/C/FINAL）。
- **FR-UI-006**：配送資訊依 `shippingMethod` 動態渲染：超商取貨顯示超商欄位，宅配顯示收件人欄位。
- **FR-UI-007**：ROLE_ADMIN 顯示「店家名稱」欄位且可篩選；ROLE_STORE_OWNER 不顯示此欄位。
- **FR-UI-008**：列表可選欄位：以 Column Visibility Toggle 讓管理員自訂顯示欄位（可選功能）。

### 狀態管理

- **SM-001**：列表載入使用 skeleton loading（5 列骨架）。
- **SM-002**：狀態更新成功後，詳情頁即時更新狀態徽章和歷程，不需重新載入頁面。
- **SM-003**：取消成功後，詳情頁狀態更新，操作按鈕消失。
- **SM-004**：篩選條件變更時自動查詢（關鍵字 debounce 500ms）。
- **SM-005**：403 錯誤時，顯示「您沒有權限存取此資源」並導向訂單列表頁。

---

## API 驗證清單

- **AV-001** ✅：`POST /api/admin/orders/list` 支援 `storeId` 篩選，Admin 可指定，StoreOwner 後端自動套用 — 前端僅 Admin 顯示店家篩選下拉。
- **AV-002** ✅：`GET /api/admin/orders/{id}` Response 包含 `statusHistory` 陣列 — 時間軸渲染有資料來源。
- **AV-003** ⚠️：`statusHistory[].operator` 格式 — 後端 spec 說明記錄「operator ID」，但前端需顯示 Email 或名稱；需確認 Response 是否包含 `operatorEmail` 或 `operatorName`。
- **AV-004** ⚠️：`prizeCount` vs `prizes[]` — 列表 Response 包含 `prizeCount` 欄位，詳情才有 `prizes` 陣列；需確認列表不會回傳完整 prizes 陣列（效能考量）。
- **AV-005** ❌：`totalValue` 欄位 — 列表 Response spec 包含 `totalValue`，但後端業務邏輯（如何計算訂單總值）未說明；若訂單以「個別獎品兌換值」加總，需確認後端確有此欄位。
- **AV-006** ✅：`POST /api/admin/orders/{id}/cancel` 需傳 `reason`，前端驗證原因必填 — 一致。
- **AV-007** ⚠️：分頁參數 — `POST /api/admin/orders/list` 未明確說明支援分頁（`page`, `size`）；訂單列表資料量預期較大，需確認。
- **AV-008** ❌：`GET /api/admin/stores/options` — 後端 spec 在 014-store-management 中提供此端點，但 008-order-management 的 API list 未提及；前端需跨 feature 呼叫此端點，需確認 Admin 在訂單管理頁可呼叫此 API。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口或不一致

---

## 成功標準

### 可量化的成果

- **SC-001**：管理員可在 3 步驟內（列表 → 詳情 → 更新狀態）完成一筆訂單狀態更新。
- **SC-002**：訂單列表（50 筆）載入時間不超過 2 秒。
- **SC-003**：ROLE_STORE_OWNER 登入後，列表 100% 只顯示自家訂單（無跨店資料洩漏）。
- **SC-004**：狀態歷程時間軸顯示所有歷史操作記錄，可用於客服糾紛處理。
- **SC-005**：Admin 可透過「店家名稱 + 訂單狀態 + 日期」組合篩選精確定位訂單。
- **SC-006**：取消訂單後，前端確認狀態顯示為「已取消」，且操作按鈕均 disabled。
