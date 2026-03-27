# 前端規格書（前台）：最新消息

**功能分支**：`cli/007-news-management`
**對應後端 Spec**：`specs/007-news-management/spec.md`
**建立日期**：2026-03-27
**狀態**：草稿
**介面類型**：前台用戶介面（Client App）
**存取角色**：公開（無需登入）

---

## 頁面與介面清單

### 頁面 1 — 消息列表頁
- **路由**：`/news`
- **存取權限**：公開
- **UI 元件**：
  - 分類 Tab 列（全部 / 公告 / 活動 / 系統）
  - 重要消息置頂橫幅（Important News Banner）
  - 消息卡片列表（News Card List）
  - 每張卡片：封面圖、分類標籤、重要標記、標題、摘要、發布時間
  - 搜尋框（關鍵字搜尋）
  - 空狀態插圖（無消息時）
  - 載入更多按鈕（或無限捲動）

### 頁面 2 — 消息詳情頁
- **路由**：`/news/{id}`
- **存取權限**：公開
- **UI 元件**：
  - 消息標題（大字號）
  - 分類標籤
  - 重要標記 Badge
  - 封面圖（寬版）
  - 發布時間與作者
  - 正文內容（支援 HTML 富文本渲染）
  - 返回列表按鈕
  - 相關消息推薦（同分類最新 3 筆）

---

## 使用者情境與測試

### 使用者故事 1 — 瀏覽消息列表並依分類篩選（優先級：P1）

玩家進入消息頁，瀏覽最新公告，並依分類快速篩選所需資訊。

**此優先級的原因**：消息是玩家了解活動、系統更新的唯一官方渠道，清晰的分類與重要標記直接影響訊息傳達效果。

**獨立測試**：
1. 進入 `/news`，確認列表顯示。
2. 點擊「活動」分類 Tab，確認只顯示 `category = EVENT` 的消息。

**驗收情境**：
1. **在** 消息列表頁載入，**當** API 回應，**則** 消息卡片依 `publishTime` 降序排列（最新消息在最上方）。
2. **在** 分類 Tab 顯示，**當** 玩家點擊「公告」，**則** 自動呼叫 `POST /api/news/list`（body: `{ condition: { category: "ANNOUNCEMENT" }, page: 1, size: 20 }`），列表更新。
3. **在** 列表中有 `isImportant = true` 的消息，**當** 頁面顯示，**則** 該消息顯示紅色「重要」Badge，且在列表頂部優先顯示（前端排序：重要 > 普通，同級依 publishTime）。
4. **在** 分類無消息，**當** API 返回空陣列，**則** 顯示「目前沒有此分類的消息」插圖與文字，不顯示錯誤。
5. **在** 搜尋框，**當** 玩家輸入關鍵字後停頓 500ms（debounce），**則** 自動觸發 API 搜尋（condition.keyword），搜尋中顯示 Spinner。

### 使用者故事 2 — 閱讀消息詳情（優先級：P1）

玩家點擊消息卡片，進入詳情頁閱讀完整內容。

**此優先級的原因**：詳情頁是消息的主要閱讀場景，內容渲染品質直接影響閱讀體驗。

**獨立測試**：
1. 點擊列表中任一消息卡片，確認導向 `/news/{id}`。
2. 確認正文 HTML 內容正確渲染，不顯示原始標籤。

**驗收情境**：
1. **在** 玩家點擊消息卡片，**當** 導向詳情頁，**則** 呼叫 `GET /api/news/{id}` 取得完整內容，頁面顯示：標題、分類、重要標記（如有）、封面圖、作者、發布時間、正文。
2. **在** 正文包含 HTML 標籤，**當** 渲染，**則** 使用安全的 HTML 渲染套件（如 DOMPurify + innerHTML），支援：標題（h2-h4）、段落（p）、粗體（strong）、連結（a，在新 tab 開啟）、圖片（img）。
3. **在** 詳情頁顯示，**當** 消息有封面圖（`imageUrl`），**則** 以 16:9 比例顯示在標題下方，載入前顯示骨架屏。
4. **在** 返回按鈕，**當** 玩家點擊，**則** 返回 `/news` 並保留之前的分類 Tab 選擇與捲動位置（使用瀏覽器歷史）。
5. **在** 詳情頁底部，**當** 頁面載入，**則** 顯示最多 3 筆同分類的最新消息（客戶端從已載入資料中過濾，或另行 API 請求）。

### 使用者故事 3 — 重要消息快速識別（優先級：P2）

重要消息在列表中有明顯視覺突顯，確保玩家不會錯過。

**驗收情境**：
1. **在** 列表中有多筆重要消息，**當** 頁面渲染，**則** 所有 `isImportant = true` 的消息卡片顯示紅色框線或橘色背景以突顯。
2. **在** 消息列表，**當** 有未讀重要消息，**則** Tab 導航列的「消息」圖示顯示紅點提示（若有全域未讀狀態管理）。

### 使用者故事 4 — 行動裝置閱讀體驗（優先級：P2）

玩家在手機閱讀消息詳情，正文圖片自適應螢幕寬度。

**驗收情境**：
1. **在** 手機瀏覽詳情頁，**當** 正文含圖片，**則** 圖片寬度 100%、高度自動，不超出螢幕邊界。
2. **在** 手機版列表，**當** 消息卡片顯示，**則** 採用全寬卡片（非格子布局），封面圖在上、文字在下。

---

## API 串接規格

### 消息列表與詳情

| 動作 | Method | 端點 | 主要 Request 欄位 | 主要 Response 欄位 | 前端 UI 狀態 |
|------|--------|------|------------------|--------------------|-------------|
| 取得消息列表 | POST | `/api/news/list` | `condition.category`, `condition.isImportant`, `condition.keyword`, `page`, `size` | `id`, `title`, `summary`, `category`, `categoryName`, `isImportant`, `imageUrl`, `publishTime` | 列表載入 |
| 取得消息詳情 | GET | `/api/news/{id}` | 無 | `id`, `title`, `content`, `category`, `isImportant`, `imageUrl`, `publishTime`, `author` | 詳情頁渲染 |

#### 列表 Request 範例
```json
POST /api/news/list
{
  "condition": {
    "category": "ANNOUNCEMENT",
    "keyword": ""
  },
  "page": 1,
  "size": 20
}
```

#### 列表 Response 範例
```json
[
  {
    "id": "news-uuid-1",
    "title": "2026 春季活動開跑！",
    "summary": "全新一番賞系列上架，活動期間抽獎加碼送 Bonus...",
    "category": "EVENT",
    "categoryName": "活動",
    "isImportant": true,
    "imageUrl": "https://cdn.example.com/news/spring2026.jpg",
    "publishTime": "2026-03-20T10:00:00"
  }
]
```

---

## 功能需求（前端 UI）

### 消息列表頁

- **FR-UI-001**：分類 Tab 列固定在頁面頂部（sticky），捲動時不消失。
- **FR-UI-002**：分類 Tab 項目：全部（ALL）/ 公告（ANNOUNCEMENT）/ 活動（EVENT）/ 系統（SYSTEM）。
- **FR-UI-003**：消息卡片在桌機為兩欄格子、手機為單欄。
- **FR-UI-004**：`isImportant = true` 的消息卡片加上紅色「！重要」Badge（左上角或標題旁）。
- **FR-UI-005**：消息卡片的 `summary` 最多顯示 2 行（超出以「...」截斷），使用 CSS `line-clamp`。
- **FR-UI-006**：搜尋框使用 debounce（500ms）避免頻繁 API 請求，搜尋中顯示小 Spinner。
- **FR-UI-007**：分類 Tab 切換時不觸發頁面捲動，列表平滑更新（使用 transition 動畫）。

### 消息詳情頁

- **FR-UI-008**：正文（`content`）使用 `v-html` 或 `dangerouslySetInnerHTML` 渲染，但必須先通過 DOMPurify 消毒，防止 XSS 攻擊。
- **FR-UI-009**：正文中的圖片自動添加 `max-width: 100%; height: auto` 樣式，確保行動裝置適配。
- **FR-UI-010**：正文中的外部連結添加 `target="_blank" rel="noopener noreferrer"` 屬性。
- **FR-UI-011**：詳情頁標題使用 `<h1>` 語義標籤，供 SEO 優化。
- **FR-UI-012**：`publishTime` 以「YYYY/MM/DD HH:mm」格式顯示；超過 24 小時的以「X 天前」相對時間顯示，懸停 Tooltip 顯示絕對時間。

### 狀態管理

- **SM-001**：`activeCategory` — 當前選中的分類 Tab（`ALL` | `ANNOUNCEMENT` | `EVENT` | `SYSTEM`）。
- **SM-002**：`newsItems` — 消息列表陣列，切換分類時替換。
- **SM-003**：`searchKeyword` — 搜尋關鍵字字串，用 debounce 觸發 API。
- **SM-004**：`currentPage` — 當前分頁頁碼，用於載入更多。
- **SM-005**：`newsDetail` — 當前詳情頁的完整消息物件。

---

## API 驗證清單

- **AV-001** ✅：`POST /api/news/list` 支援 `category` 篩選，可傳入 `"ALL"` 取得全部分類 — 已確認。
- **AV-002** ✅：Response 包含 `isImportant` 欄位，前端可顯示重要標記 — 已確認。
- **AV-003** ✅：`GET /api/news/{id}` 返回 `content` 欄位（完整 HTML 富文本）— 已確認。
- **AV-004** ⚠️：`POST /api/news/list` 的 Response 是否包含分頁 metadata（`totalElements`, `totalPages`）？若無，前端無法知道是否還有更多消息可載入。
- **AV-005** ⚠️：`categoryName` 是後端返回的中文（如「活動」）還是英文枚舉（如 `EVENT`）？前端需知道是否自行 i18n 映射。
- **AV-006** ❌：Response 未包含 `readCount`（閱讀次數）或 `likes` 欄位 — 若未來需顯示熱門消息指標，需後端補充。
- **AV-007** ⚠️：詳情頁的 `author` 欄位是管理員暱稱還是系統帳號？需確認顯示格式。
- **AV-008** ❌：消息列表 Response 不包含 `totalCount` 或 `hasMore` 欄位 — 分頁的「載入更多」按鈕需要知道是否還有下一頁，建議後端補充。
- **AV-009** ⚠️：當分類 Tab 為「全部（ALL）」時，Request body 的 `condition.category` 應傳 `"ALL"` 還是省略此欄位？需確認後端 API 的行為。

凡例：✅ 已確認一致 | ⚠️ 需要確認 | ❌ 發現缺口

---

## 成功標準

- **SC-001**：消息列表在 20 筆資料下首次渲染 ≤ 1 秒。
- **SC-002**：分類 Tab 切換後列表更新 ≤ 500ms（API 回應 + 渲染）。
- **SC-003**：詳情頁正文 HTML 通過 DOMPurify 安全渲染，無 XSS 漏洞（通過 OWASP 基礎掃描）。
- **SC-004**：手機版消息詳情頁正文圖片不溢出螢幕邊界（100% 寬度自適應）。
- **SC-005**：重要消息在列表中視覺突顯，與普通消息有明顯區別（用戶測試可識別率 ≥ 90%）。
- **SC-006**：搜尋功能使用 500ms debounce，每次鍵盤輸入不觸發多餘 API 請求。
