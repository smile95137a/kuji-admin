# 前端規格書（前台）：店家管理

**功能分支**：`cli/014-store-management`
**對應後端 Spec**：`specs/014-store-management/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：公開（無需登入）

---

## 頁面與介面清單

### 頁面 1 — 店家列表頁
- **路由**：`/stores`
- **存取權限**：公開
- **UI 元件**：
  - 頁面標題（合作店家）
  - 關鍵字搜尋框（即時篩選）
  - 店家卡片格子（Store Card Grid）
  - 每張卡片：店家 Logo、店家名稱、簡介（shortDescription）
  - 空狀態插圖（無已啟用店家時）
  - 骨架屏（API 載入中）

### 頁面 2 — 店家詳情頁
- **路由**：`/stores/{id}`
- **存取權限**：公開
- **UI 元件**：
  - 封面橫幅（coverImageUrl，寬版）
  - 店家 Logo（coverImageUrl 上方或左側）
  - 店家名稱（大標題）
  - 簡介文字（shortDescription 與 longDescription）
  - 聯絡資訊卡（電話、Email、地址）
  - 營業時間卡片（businessHours）
  - 社群媒體連結按鈕（Facebook、Instagram）
  - 商品列表區塊（此店家的 ON_SHELF 商品）
  - 商品卡片格子（與 /lottery 頁面相同樣式）
  - 返回店家列表按鈕

---

## 使用者情境與測試

### 使用者故事 1 — 瀏覽所有合作店家（優先級：P1）

訪客進入店家列表頁，瀏覽所有已啟用的合作店家。

**此優先級的原因**：店家是玩家探索商品的重要維度（以店家為單位發現商品），同時是來自首頁橫幅點擊的著陸頁。

**獨立測試**：
1. 進入 `/stores`，確認店家卡片顯示。
2. 確認只顯示 `status = ENABLED` 的店家（後端已過濾，前端確認數量合理）。

**驗收情境**：
1. **在** 訪客進入 `/stores`，**當** `GET /api/stores` 回應，**則** 顯示所有已啟用店家的卡片（後端只返回 `status = ENABLED` 的店家）。
2. **在** 店家卡片，**當** 顯示，**則** 包含：店家 Logo（或預設 Logo 佔位圖）、店家名稱（最多 2 行）、簡介文字（最多 3 行截斷）。
3. **在** 點擊任一店家卡片，**當** 玩家點擊，**則** 導向 `/stores/{id}` 店家詳情頁。
4. **在** `GET /api/stores` 返回空陣列，**當** 頁面渲染，**則** 顯示空狀態插圖與「目前暫無合作店家，敬請期待！」文字。
5. **在** 搜尋框，**當** 玩家輸入店家名稱關鍵字，**則** 客戶端即時篩選卡片（不重新呼叫 API），篩選響應 ≤ 50ms。

### 使用者故事 2 — 查看店家詳情與商品（優先級：P1）

玩家進入特定店家詳情頁，了解店家資訊並瀏覽該店家所有在售商品。

**此優先級的原因**：店家詳情頁是從首頁橫幅引流的主要著陸頁，內容豐富度直接影響玩家探索商品的意願。

**獨立測試**：
1. 點擊一個有商品的店家卡片，進入詳情頁。
2. 確認店家資訊與商品列表均正確顯示。

**驗收情境**：
1. **在** 玩家進入 `/stores/{id}`，**當** `GET /api/stores/{id}` 回應，**則** 顯示店家完整資訊：封面橫幅、Logo、名稱、完整描述（longDescription）、聯絡資訊、營業時間、社群連結。
2. **在** 店家詳情頁，**當** 商品區塊載入，**則** 呼叫 `POST /api/lottery/browse/list`（condition.storeId = "{id}", condition.status = "ON_SHELF"），顯示該店家的在售商品格子。
3. **在** 商品格子，**當** 玩家點擊商品卡片，**則** 導向 `/lottery/{productId}`。
4. **在** 店家無任何在售商品，**當** 商品區塊顯示，**則** 顯示「此店家目前沒有在售商品，請稍後再來」，不顯示空的格子框架。
5. **在** `facebookUrl` 或 `instagramUrl` 不為空，**當** 社群連結按鈕顯示，**則** 點擊後在新分頁（`target="_blank"`）開啟連結。
6. **在** `facebookUrl` 和 `instagramUrl` 均為空，**當** 頁面顯示，**則** 社群連結區塊完全隱藏（不顯示空按鈕）。

### 使用者故事 3 — 從首頁橫幅點擊進入店家頁（優先級：P1）

玩家在首頁點擊橫幅，直接導向對應店家的詳情頁。

**此優先級的原因**：此流程連接 001-banner-homepage 和 014-store-management，確保橫幅引流的著陸體驗完整。

**驗收情境**：
1. **在** 玩家從首頁橫幅點擊（導向 `/stores/{storeId}`），**當** 詳情頁載入，**則** 店家資訊與商品列表均正確顯示（與直接導航的體驗一致）。
2. **在** 被導向的店家已停用，**當** `GET /api/stores/{id}` 返回 404 或 403，**則** 顯示「此店家不存在或已停止服務」提示，並提供「回到首頁」按鈕。

### 使用者故事 4 — 瀏覽店家營業時間（優先級：P3）

玩家在店家詳情頁查看實體店面的營業時間。

**驗收情境**：
1. **在** 店家有 `businessHours` 資料，**當** 頁面顯示，**則** 以可讀格式呈現（如表格或清單：週一至週五 10:00–22:00）。
2. **在** `businessHours` 為空或 null，**當** 頁面顯示，**則** 完全隱藏營業時間區塊（不顯示空的標題或框架）。

### 使用者故事 5 — 行動裝置瀏覽店家頁（優先級：P2）

玩家在手機上瀏覽店家列表和詳情頁，體驗與桌機等同。

**驗收情境**：
1. **在** 手機版店家列表，**當** 頁面顯示，**則** 卡片為 2 欄格子（而非桌機的 3–4 欄）。
2. **在** 手機版店家詳情頁，**當** 顯示商品格子，**則** 商品卡片為 2 欄格子（與 /lottery 頁面手機版一致）。
3. **在** 手機版店家詳情頁，**當** 封面橫幅顯示，**則** 高度為 160px（不過高，不擠壓內容閱讀空間）。

---

## API 串接規格

### 店家瀏覽

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得店家列表 | GET | `/api/stores` | 無 | `id`, `name`, `shortDescription`, `logoUrl`, `status` | 店家卡片格子 |
| 取得店家詳情 | GET | `/api/stores/{id}` | 無 | `id`, `name`, `shortDescription`, `longDescription`, `logoUrl`, `coverImageUrl`, `email`, `phone`, `address`, `businessHours`, `facebookUrl`, `instagramUrl`, `status` | 詳情頁渲染 |
| 取得店家商品 | POST | `/api/lottery/browse/list` | `condition.storeId`, `condition.status: "ON_SHELF"`, `page`, `size` | 商品陣列 | 店家商品格子 |
| 取得店家下拉選項 | GET | `/api/stores/options` | 無 | `id`, `name` | 搜尋/篩選下拉 |

#### 店家列表 Response 範例
```json
[
  {
    "id": "store-uuid-1",
    "name": "扭蛋王國",
    "shortDescription": "日本進口正版扭蛋，每月新品不斷",
    "logoUrl": "https://cdn.example.com/stores/gacha-kingdom-logo.jpg",
    "status": "ENABLED"
  }
]
```

#### 店家詳情 Response 範例
```json
{
  "id": "store-uuid-1",
  "name": "扭蛋王國",
  "shortDescription": "日本進口正版扭蛋，每月新品不斷",
  "longDescription": "<p>歡迎來到扭蛋王國！我們專注於...</p>",
  "logoUrl": "https://cdn.example.com/stores/logo.jpg",
  "coverImageUrl": "https://cdn.example.com/stores/cover.jpg",
  "email": "info@gacha-kingdom.com",
  "phone": "02-1234-5678",
  "address": "台北市信義區松高路1號",
  "businessHours": "週一至週日 10:00–22:00",
  "facebookUrl": "https://facebook.com/gachakingdom",
  "instagramUrl": "https://instagram.com/gachakingdom",
  "status": "ENABLED"
}
```

---

## 功能需求（前端 UI）

### 店家列表頁

- **FR-UI-001**：店家卡片格子在桌機為 3–4 欄、平板為 2–3 欄、手機為 2 欄。
- **FR-UI-002**：Logo 使用圓形裁切（`border-radius: 50%`）或圓角正方形呈現，比例 1:1。
- **FR-UI-003**：`logoUrl` 為空時顯示預設店家 Logo（品牌佔位圖）。
- **FR-UI-004**：店家卡片的 `shortDescription` 最多顯示 2–3 行（CSS `line-clamp`），超出截斷。
- **FR-UI-005**：搜尋篩選為客戶端即時過濾（不呼叫 API），過濾 `name` 和 `shortDescription` 是否包含關鍵字（不分大小寫）。

### 店家詳情頁

- **FR-UI-006**：封面橫幅（coverImageUrl）以全寬展示，高度桌機 320px、手機 160px，使用 `object-fit: cover`。
- **FR-UI-007**：`longDescription` 若含 HTML 標籤，使用安全 HTML 渲染（DOMPurify）；若為純文字，以段落（`<p>`）顯示。
- **FR-UI-008**：聯絡資訊卡顯示條件：`email` / `phone` / `address` 各自有值時才顯示對應行，全空時隱藏整個聯絡資訊卡。
- **FR-UI-009**：社群媒體連結按鈕使用對應品牌 Logo（Facebook 藍色按鈕、Instagram 漸層按鈕），點擊以 `target="_blank"` 開啟新分頁。
- **FR-UI-010**：店家商品區塊的商品卡片樣式與 `/lottery` 頁面保持一致，點擊後導向 `/lottery/{id}`。
- **FR-UI-011**：「返回店家列表」按鈕在頁面頂部（麵包屑導覽：首頁 > 店家 > {店家名稱}）。

### SEO 考量

- **FR-UI-012**：店家列表頁 `<title>` 為「合作店家 — KUJI」。
- **FR-UI-013**：店家詳情頁 `<title>` 為「{storeName} — KUJI」，`<meta description>` 使用 `shortDescription`。
- **FR-UI-014**：封面圖 `<img>` 的 `alt` 屬性為「{storeName} 封面圖片」。

### 狀態管理

- **SM-001**：`stores` — 店家列表陣列（一次性載入，客戶端搜尋）。
- **SM-002**：`storeSearchKeyword` — 客戶端搜尋關鍵字（即時過濾，無 debounce 需要）。
- **SM-003**：`filteredStores` — 計算屬性，依 `storeSearchKeyword` 過濾 `stores`。
- **SM-004**：`storeDetail` — 詳情頁當前店家物件。
- **SM-005**：`storeProducts` — 詳情頁中此店家的商品陣列。

---

## API 驗證清單

- **AV-001** ✅：`GET /api/stores` 後端只返回 `status = ENABLED` 的店家，前端無需自行過濾 — 已確認。
- **AV-002** ✅：`GET /api/stores/{id}` 返回完整店家資訊（含 `businessHours`, `facebookUrl`, `instagramUrl`）— 已確認。
- **AV-003** ✅：店家詳情頁的商品列表使用 `POST /api/lottery/browse/list` 並傳入 `condition.storeId`，與商品列表頁共用同一端點 — 已確認。
- **AV-004** ⚠️：`GET /api/stores` 的 Response 中 `status` 欄位是否一定為 `"ENABLED"`？若後端過濾後所有店家都是 ENABLED，前端不需要此欄位；但若有邊緣情況，需確認。
- **AV-005** ⚠️：`businessHours` 的資料格式是否為結構化物件（如 `{ monday: "10:00-22:00", tuesday: "..." }`）或純字串（如 `"週一至週日 10:00–22:00"`）？前端渲染方式取決於格式。
- **AV-006** ❌：`longDescription` 是否為 HTML 富文本？若是，需 DOMPurify 消毒；若為純文字，直接顯示 — 需後端確認欄位的內容類型。
- **AV-007** ⚠️：`GET /api/stores/options` 端點的用途（供搜尋篩選用的下拉選單）— 在前台的用戶場景下，此端點是否被用於商品列表頁的「依店家篩選」功能？需確認此端點在前台的使用場景。
- **AV-008** ❌：`GET /api/stores/{id}` 若店家不存在或已停用，Response 的 HTTP 狀態碼是 404 還是 403？前端需根據狀態碼顯示對應的錯誤頁面（「找不到此店家」vs「此店家已停止服務」）。
- **AV-009** ⚠️：店家詳情頁的商品列表是否有分頁？`POST /api/lottery/browse/list` 的 Response 需返回 `hasMore` 欄位，供「載入更多」功能判斷。
- **AV-010** ❌：`GET /api/stores` Response 缺少 `productCount`（在售商品數量）欄位 — 店家卡片若要顯示「共 X 件商品」幫助玩家快速評估店家豐富度，需後端補充或前端另行計算。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 響應式設計規格

| 頁面 | 桌機（≥1024px） | 平板（768–1023px） | 手機（<768px） |
|------|----------------|-------------------|---------------|
| 店家列表 | 4 欄格子 | 3 欄格子 | 2 欄格子 |
| 店家封面橫幅 | 320px 高 | 240px 高 | 160px 高 |
| 店家商品格子 | 4 欄 | 2 欄 | 2 欄 |
| 詳情資訊布局 | 橫向排列（資訊+商品）| 垂直排列 | 垂直排列 |

---

## 成功標準

- **SC-001**：店家列表頁在 20 家店家下首次渲染 ≤ 1 秒（含 API 回應）。
- **SC-002**：客戶端店家搜尋過濾響應 ≤ 50ms（純計算，無 API 呼叫）。
- **SC-003**：店家詳情頁（含商品列表）完整渲染 ≤ 2 秒（兩個 API 並行呼叫）。
- **SC-004**：店家詳情頁 `GET /api/stores/{id}` 和 `POST /api/lottery/browse/list` 兩個 API 並行呼叫（Promise.all），不等待第一個完成後才發第二個請求。
- **SC-005**：社群媒體連結點擊後在新分頁開啟，不跳離當前店家詳情頁。
- **SC-006**：封面橫幅載入失敗時（網路錯誤），店家名稱和其他資訊仍正常顯示（封面區域以品牌預設顏色填充），不產生版面破損。
