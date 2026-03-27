# 前端規格書（後台）：商品與抽獎管理

**功能分支**：`011-product-lottery`
**對應後端 Spec**：`specs/011-product-lottery/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：後台管理介面（Admin Panel）
**存取角色**：ROLE_ADMIN、ROLE_STORE_OWNER、ROLE_STORE_EDITOR

---

## 頁面與介面清單

### 頁面 1 — 抽獎商品列表頁
- **路由**：`/admin/lottery`
- **存取權限**：ROLE_ADMIN、ROLE_STORE_OWNER、ROLE_STORE_EDITOR（依權限設定）
- **UI 元件**：
  - 頁面標題「抽獎商品管理」
  - 篩選列：
    - 類別下拉（全部 / 官方一番賞 / 扭蛋 / 集換卡牌 / 自訂扭蛋）
    - 子類別下拉（自訂扭蛋時顯示：抽獎模式 / 刮刮樂模式）
    - 狀態下拉（全部 / 草稿 / 已配置 / 上架中 / 可抽 / 進行中 / 售完 / 已下架）
    - 關鍵字搜尋（商品名稱）
    - 日期範圍（建立時間）
    - 排序下拉
  - 「新增商品」按鈕（右上角）
  - 資料表格：
    - 欄位：縮圖、商品名稱、店家名稱（Admin）、類別/子類別、模式、單次價格、剩餘次數/總次數、狀態徽章、開始時間、操作
    - 操作：查看詳情、獎品設定、上架／下架、複製商品、刪除（草稿限定）

### 頁面 2 — 新增 / 編輯抽獎商品頁
- **路由**：`/admin/lottery/create` 或 `/admin/lottery/{id}/edit`
- **存取權限**：ROLE_ADMIN、ROLE_STORE_OWNER
- **UI 元件**：
  - 頁面標題「新增抽獎商品」／「編輯抽獎商品」
  - 基本資訊區塊：
    - 商品名稱（必填）
    - 類別選擇（必選，單選）
    - 子類別（選類別「自訂扭蛋」後必選：抽獎模式 / 刮刮樂模式）
    - 遊玩模式（依類別自動帶入，部分類別固定）
    - 店家選擇（ROLE_ADMIN 可選；ROLE_STORE_OWNER 自動帶入，唯讀）
    - 商品描述富文本（可選）
  - 圖片設定：封面圖片上傳
  - 抽獎設定區塊：
    - 單次抽獎價格（金幣，正整數）
    - 總抽獎次數（maxDraws，正整數）
    - 批次抽獎設定（可選：如 10 抽、50 抽選項）
  - 時間設定：
    - 上架開始時間（startTime）
    - 開放抽獎時間（drawTime，可與上架時間不同）
    - 結束時間（endTime，可選）
  - 末獎設定（可選）：
    - 模式：傳統末獎 / N+1 獎池
    - 關聯獎品 ID 選擇（需先設定獎品後才可選）
  - 自動折扣設定（可選）：啟用 Toggle + 折扣條件設定
  - 儲存草稿 / 儲存並配置獎品 / 取消按鈕

### 頁面 3 — 獎品管理頁
- **路由**：`/admin/lottery/{id}/prizes`
- **存取權限**：ROLE_ADMIN、ROLE_STORE_OWNER
- **UI 元件**：
  - 頁面標題「商品名稱 — 獎品管理」
  - 返回商品列表按鈕
  - 商品概覽（商品名稱、總抽獎次數、已設定獎品數量）
  - 「新增獎品」按鈕
  - 獎品列表（卡片或表格）：
    - 欄位：圖片、獎品名稱、等級徽章（A/B/C/D/FINAL）、數量、剩餘數量、回收點數、操作
    - 操作：編輯、刪除
  - 獎品數量加總提示（vs maxDraws）
  - 「設定末獎」按鈕
  - 「完成配置」按鈕（全部獎品數量等於 maxDraws 時啟用）

### 頁面 4 — 新增 / 編輯獎品 Modal
- **路由**：`/admin/lottery/{id}/prizes`（Modal 覆蓋）
- **存取權限**：ROLE_ADMIN、ROLE_STORE_OWNER
- **UI 元件**：
  - Modal 標題「新增獎品」／「編輯獎品」
  - 獎品圖片上傳
  - 獎品名稱（必填）
  - 等級選擇（A / B / C / D / FINAL）
  - 數量（正整數，必填）
  - 回收點數（玩家不提取時可獲得的點數，正整數）
  - 描述（可選）
  - 儲存 / 取消

### 頁面 5 — 商品詳情頁（唯讀）
- **路由**：`/admin/lottery/{id}`
- **存取權限**：ROLE_ADMIN、ROLE_STORE_OWNER、ROLE_STORE_EDITOR
- **UI 元件**：
  - 商品基本資訊（唯讀卡片）
  - 商品狀態徽章 + 操作按鈕（上架／下架）
  - 獎品列表（唯讀，顯示剩餘數量）
  - 抽獎進度條（已抽 / 總次數）

---

## 使用者情境與測試

### 使用者故事 1 — 店家管理員新增一個「官方一番賞」商品並完成獎品配置（優先級：P1）

店家管理員建立新抽獎商品，設定基本資訊、獎品後，完成配置並上架供玩家抽獎。

**此優先級的原因**：商品建立是抽獎平台的核心流程，阻塞所有下游功能。

**獨立測試**：StoreOwner 帳號登入，建立類別「官方一番賞」商品，新增 3 個等級的獎品，完成配置並上架。

**驗收情境**：
1. **在** 商品新增頁面，**當** 填寫名稱、選擇類別「官方一番賞」、設定價格 10 金幣、總次數 30 並儲存，**則** 商品以「草稿」狀態出現在列表。
2. **在** 草稿商品，**當** 進入獎品管理並新增總數量等於 30 的獎品，點擊「完成配置」，**則** 商品狀態升為「已配置」，「上架」按鈕啟用。
3. **在** 獎品總數量不等於 maxDraws，**當** 點擊「完成配置」，**則** 顯示錯誤「獎品總數量（{n}）必須等於總抽獎次數（{maxDraws}）」。

---

### 使用者故事 2 — Admin 強制下架進行中的商品（優先級：P1）

發現違規或錯誤的商品，Admin 需要立即強制下架，不論商品當前狀態。

**此優先級的原因**：緊急下架是後台管控的關鍵安全機制。

**驗收情境**：
1. **在** 商品狀態為「進行中」，**當** Admin 點擊「強制下架」並確認，**則** 商品狀態變為「已下架」，前台無法繼續抽獎。
2. **在** ROLE_STORE_OWNER 查看進行中商品，**當** 依其權限僅有「可編輯」，**則** 也能對自家商品執行下架操作（確認業務權限設定）。

---

### 使用者故事 3 — 管理員複製現有商品（優先級：P2）

複製一個已存在的商品作為新商品的基礎，減少重複輸入工作。

**此優先級的原因**：提升作業效率，但非核心阻塞功能。

**驗收情境**：
1. **在** 任何狀態的商品，**當** 管理員點擊「複製商品」，**則** 建立一個新的「草稿」商品，包含原商品的所有設定（獎品列表、基本資訊），但 `remainingDraws` 重置為 `maxDraws`，狀態為草稿。
2. **在** 複製後，**當** 進入新商品編輯頁，**則** 商品名稱預填為「{原商品名稱} - 複製」。

---

### 使用者故事 4 — 管理員設定末獎機制（優先級：P2）

特定商品需要設定「最後一抽」的特殊獎品，吸引玩家堅持抽到最後。

**驗收情境**：
1. **在** 獎品管理頁，**當** 點擊「設定末獎」，選擇模式「傳統末獎」並指定 FINAL 等級獎品，**則** 末獎設定儲存，前台顯示末獎資訊。
2. **在** 末獎模式為「N+1 獎池」，**當** 管理員設定，**則** 系統說明此模式意義（超出 maxDraws 的額外機會）。

---

### 邊界情況

- 刪除「草稿」以外狀態的商品，顯示「只能刪除草稿狀態的商品」。
- 上架商品的獎品不得刪除（顯示「商品上架中，無法刪除獎品」）。
- 商品 `remainingDraws` 為 0 時，自動顯示「售完」狀態，前台停止接受抽獎。
- 批次抽獎次數設定超過 `remainingDraws`，前台需處理（後端業務邏輯），後台設定時需提示。

---

## API 串接規格

### 商品管理

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 查詢商品列表 | POST | `/api/admin/lottery/list` | `condition:{category, status, keyword, startDate, endDate}, sortBy, sortOrder` | `[{id, storeId, storeName, title, category, subCategory, playMode, pricePerDraw, maxDraws, remainingDraws, status, imageUrl, startTime}]` | loading → 表格渲染 |
| 新增商品 | POST | `/api/admin/lottery` | `title, description, category, subCategory, playMode, pricePerDraw, maxDraws, imageUrl, startTime, endTime, storeId, weight` | `{id, storeId, title, status:"DRAFT"}` | 按鈕 loading → Toast → 導向列表 |
| 更新商品 | PUT | `/api/admin/lottery/{id}` | 同上 | `{id, ...}` | 按鈕 loading → Toast |
| 刪除商品 | DELETE | `/api/admin/lottery/{id}` | — | `{success: true}` | 確認 Dialog → loading → 列表移除 |
| 複製商品 | POST | `/api/admin/lottery/{id}/copy` | — | `{id, title, status:"DRAFT"}` | 按鈕 loading → Toast → 列表新增 |
| 變更商品狀態 | PUT | `/api/admin/lottery/{id}/status` | `status: "ON_SHELF"\|"OFF_SHELF"` | `{id, status}` | 按鈕 loading → 狀態更新 |

### 獎品管理

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得獎品列表 | GET | `/api/admin/lottery/{id}/prizes` | — | `[{id, name, level, quantity, remaining, imageUrl, recycleBonus}]` | loading → 列表渲染 |
| 新增獎品 | POST | `/api/admin/lottery/{id}/prizes` | `name, level, quantity, imageUrl, recycleBonus, description` | `{id, name, level, quantity}` | Modal loading → Toast → 列表更新 |
| 更新獎品 | PUT | `/api/admin/prize/{id}` | `name, level, quantity, imageUrl, recycleBonus, description` | `{id, ...}` | Modal loading → Toast |
| 刪除獎品 | DELETE | `/api/admin/prize/{id}` | — | `{success: true}` | 確認 Dialog → loading → 列表移除 |
| 設定末獎 | POST | `/api/admin/lottery/{id}/final-prize` | `mode:"TRADITIONAL"\|"POOL", prizeId` | `{success: true}` | 按鈕 loading → Toast |

---

## 功能需求（前端 UI）

### 介面需求

- **FR-UI-001**：商品狀態徽章：草稿（灰）、已配置（藍）、上架中（青）、可抽（紫）、進行中（橘）、售完（紅）、已下架（深灰）。
- **FR-UI-002**：獎品等級徽章：FINAL（金/彩虹）、A（紅）、B（橘）、C（藍）、D（灰）。
- **FR-UI-003**：獎品管理頁顯示「目前總數：{sum} / {maxDraws}」進度條，超過或不足時以紅色顯示。
- **FR-UI-004**：商品類別與子類別有聯動：選擇「自訂扭蛋」才顯示子類別選單；其他類別子類別隱藏。
- **FR-UI-005**：ROLE_STORE_OWNER 新增商品時，店家欄位自動帶入並設為唯讀；ROLE_ADMIN 顯示店家下拉選擇器。
- **FR-UI-006**：「完成配置」按鈕只有在「獎品總數 = maxDraws」時才 enabled，否則 disabled 並 tooltip 說明原因。
- **FR-UI-007**：進行中商品的獎品列表顯示「剩餘：{remaining}」，已抽出的獎品以刪除線樣式標示（可選展示）。
- **FR-UI-008**：抽獎進度條（已抽次數 / 總次數）以百分比視覺化顯示。

### 狀態管理

- **SM-001**：商品列表初始載入使用 skeleton loading。
- **SM-002**：獎品管理頁的獎品數量加總即時計算（前端，不需 API）。
- **SM-003**：商品上架成功後，Toast「商品已上架」，狀態徽章即時更新。
- **SM-004**：複製商品成功後，Toast「商品複製成功」，列表新增一筆草稿商品。
- **SM-005**：篩選條件變更自動觸發查詢（關鍵字 debounce 300ms）。

---

## API 驗證清單

- **AV-001** ✅：`PUT /api/admin/lottery/{id}/status` 支援 `ON_SHELF`/`OFF_SHELF` — 上下架操作對應明確。
- **AV-002** ⚠️：商品狀態機完整流程 — 後端定義 7 個狀態，但 API 只提供 `ON_SHELF`/`OFF_SHELF` 兩個可操作狀態；其他狀態（DRAFT → CONFIGURED → SOLDOUT）是系統自動轉換還是另有 API？需與後端確認。
- **AV-003** ✅：`POST /api/admin/lottery/{id}/copy` 複製端點存在 — 商品複製功能有 API 支援。
- **AV-004** ❌：批次抽獎設定欄位 — 新增商品 API 的 `Body` 未提及批次抽獎（如允許 10 抽、50 抽的設定欄位）；前端 UI 設計此功能但 API spec 未體現，需確認欄位名稱（如 `batchDrawOptions`）。
- **AV-005** ⚠️：`weight` 欄位含義 — 新增商品 Body 中有 `weight` 欄位，但後端 spec 未說明用途（商品排列權重？）；需確認是否需要前端 UI 對應輸入。
- **AV-006** ❌：「完成配置」觸發 API — 後端無獨立的「完成配置」端點；狀態從 DRAFT → CONFIGURED 的觸發機制未說明（獎品數量滿足時自動升級？還是需要前端呼叫特定 API？）。
- **AV-007** ⚠️：自動折扣設定欄位 — 後端 spec FR-009 提及「高等獎品售完後自動降價」，但新增商品 API Body 未含相關設定欄位；前端 UI 需確認是否有此設定或為後端自動邏輯。
- **AV-008** ✅：`DELETE /api/admin/prize/{id}` 在獎品層級操作 — 獎品獨立刪除有端點支援。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口或不一致

---

## 成功標準

### 可量化的成果

- **SC-001**：店家管理員可在 10 分鐘內完成一個抽獎商品從建立到上架的完整流程。
- **SC-002**：商品列表在 50 筆資料內，載入時間不超過 2 秒。
- **SC-003**：獎品管理頁的數量加總計算為即時（前端計算，0ms 延遲）。
- **SC-004**：商品複製後，新商品出現在列表頁不超過 2 秒（API + UI 更新）。
- **SC-005**：所有商品狀態轉換在前台對應的操作按鈕正確顯示（狀態機一致性 100%）。
- **SC-006**：ROLE_STORE_EDITOR 依權限設定，不得存取商品新增 / 刪除功能（按鈕不顯示或 disabled）。
