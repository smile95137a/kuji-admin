# PROMPT - 給後台前端開發者的溝通文件

> 此 Prompt 可直接複製給前端開發者，或作為需求溝通的基礎。

---

## 📋 Prompt 範本（複製給前端）

---

你好，我是後端負責人。以下是 **KUJI 後台管理系統（Admin Panel）** 的後端實際業務邏輯整理，請根據此文件實作前端。

### 技術棧需求
- **框架**：Vue 3 + TypeScript
- **UI 元件庫**：建議 Element Plus 或 Ant Design Vue
- **狀態管理**：Pinia
- **路由**：Vue Router 4（動態路由）
- **HTTP 客戶端**：Axios（需含 Token 刷新攔截器）

---

### 1. 基礎設定

**API Base URL**
```
開發環境: http://localhost:8080/api
生產環境: （由後端提供）
```

**認證**
- 所有後台 API 需 Header：`Authorization: Bearer {accessToken}`
- Token 過期（401）自動用 refreshToken 換新 Token，無縫刷新
- refreshToken 也過期則導向登入頁

**回應格式（後端統一包裝）**
```typescript
interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  error: { code: string; message: string } | null;
  meta: { timestamp: string; requestId: string };
}
```

---

### 2. 角色與權限（關鍵）

系統有 3 個後台角色：

| 角色代碼 | 中文名稱 | 特點 |
|---------|---------|------|
| `ROLE_ADMIN` | 系統管理員 | 可操作全部功能，跨所有店家 |
| `ROLE_STORE_OWNER` | 店家負責人 | 只能操作自己店家的資料 |
| `ROLE_STORE_EDITOR` | 店家編輯 | 只能操作商品和部分訂單，無報表 |

**關鍵設計**：登入後 API 回傳 `menus`（選單樹），前端根據這個樹動態生成側邊選單與頁面路由。每個節點有 `permissions.canView`/`canEdit`/`canDelete`，控制頁面的按鈕顯示。

---

### 3. StoreID 自動注入（重要！）

StoreOwner / StoreEditor 呼叫商品、訂單相關 API 時，**不需要傳 storeId**，後端從 JWT Token 自動解析並過濾。

只有 Admin 新增商品時，才需要在 body 傳 `storeId`（因為 Admin 沒有預設店家）。

```javascript
// StoreOwner 新增商品 - 不傳 storeId
const res = await axios.post('/api/admin/lottery-with-prizes', {
  lottery: {
    title: '鬼滅一番賞',
    category: 'OFFICIAL_ICHIBAN',
    pricePerDraw: 80,
    totalDraws: 100
  },
  prizes: [ ... ]
});

// Admin 新增商品 - 必須傳 storeId
const res = await axios.post('/api/admin/lottery-with-prizes', {
  lottery: {
    storeId: 'store-uuid',  // Admin 必須指定
    title: '鬼滅一番賞',
    ...
  },
  prizes: [ ... ]
});
```

---

### 4. 查詢 API 模式

後台所有列表查詢使用 `POST + /list` 格式：

```javascript
// 商品查詢範例
const res = await axios.post('/api/admin/lottery/list', {
  condition: {
    status: 'ON_SHELF',
    keyword: '鬼滅'          // 後端模糊搜尋
  },
  sortBy: 'created_at',
  sortOrder: 'DESC'
});

// 後端返回所有資料（無分頁），前端自己做分頁
const all = res.data.data;
const page1 = all.slice(0, 20);
```

---

### 5. 商品類型（關鍵業務邏輯）

後台建立商品時，`category` + `subCategory` + `gameMode` 三個欄位決定商品類型：

```javascript
// 一番賞、扭蛋、卡牌：只傳 category
{ category: 'OFFICIAL_ICHIBAN' }
{ category: 'GACHA' }
{ category: 'TRADING_CARD' }

// 自製賞-抽籤型
{ category: 'CUSTOM_GACHA', subCategory: 'LOTTERY_MODE' }

// 自製賞-刮刮樂（全隨機）
{ category: 'CUSTOM_GACHA', subCategory: 'SCRATCH_MODE', gameMode: 'RANDOM' }

// 自製賞-刮刮樂（店家指定大獎）
{ category: 'CUSTOM_GACHA', subCategory: 'SCRATCH_MODE', gameMode: 'SCRATCH_STORE' }

// 自製賞-刮刮樂（玩家指定大獎）
{ category: 'CUSTOM_GACHA', subCategory: 'SCRATCH_MODE', gameMode: 'SCRATCH_PLAYER' }
```

⚠️ **`playMode` 無論任何商品類型都不傳**（包括刮刮樂）。後端根據 `subCategory` 自動推算：
   - `subCategory = SCRATCH_MODE` → `playMode` 自動設為 `SCRATCH_MODE`
   - 其他情況 → `playMode` 自動設為 `LOTTERY_MODE`

---

### 6. 商品的統一建立 API

推薦使用整合 API（一次完成商品 + 獎品）：

```
POST /api/admin/lottery-with-prizes
PUT  /api/admin/lottery-with-prizes/{id}
GET  /api/admin/lottery-with-prizes/{id}
```

---

### 7. 訂單狀態流轉

```
PENDING_PAYMENT → PAID → PREPARING → SHIPPED → DELIVERED → COMPLETED
                                    ↘ CANCELLED（需在 SHIPPED 前）
```

後端有狀態機驗證，不合法的狀態轉移會拒絕。

---

### 8. 首次登入強制改密碼

登入後若 `LoginRes.user.mustChangePassword === true`，**必須強制導向改密碼頁面**，不可繞過。

---

### 9. 文件位置

完整 API 規格文件在：`/frontend/admin/` 目錄下，含各模組詳細說明：
- `00-architecture.md` — 整體架構、錯誤碼、枚舉值
- `01-auth.md` — 登入/Token 流程
- `02-roles-and-permissions.md` — RBAC 與動態選單
- `03-store-management.md` — 店家管理
- `04-account-management.md` — 帳號管理（StoreOwner/Editor）
- `05-product-management.md` — 商品與獎品管理
- `06-order-management.md` — 訂單管理
- `07-frontend-user-management.md` — 前台玩家管理
- `08-report-analytics.md` — 報表
- `09-content-management.md` — Banner/新聞/跑馬燈
- `10-system-config.md` — 系統參數

Swagger UI（即時 API 文件）：`http://localhost:8080/api/swagger-ui.html`

---

## ❓ 需要前端確認的問題

> 以下是目前後端對前端的疑問，請確認後調整實作：

### 高優先（影響架構）

1. **動態路由策略**：  
   登入後後端回傳 `menus` 樹，前端是用「完整定義所有路由，再用 `meta.permissions` 控制顯示」，還是「根據 `menus` 動態 `addRoute`」？  
   → 影響路由設計，兩種都可，請確認方案。

2. **分頁策略**：  
   後端全部數據返回，前端做分頁。請確認前端使用客戶端分頁還是額外加 `offset`/`limit` 請求參數？  
   → 目前後端支持輕量客戶端分頁方案（全量回傳）。

3. **Token 儲存**：  
   `accessToken` 放 pinia store（頁面刷新會消失），`refreshToken` 放 localStorage。刷新頁面後是否需要自動從 localStorage 的 refreshToken 取回 accessToken？  
   → 如果是，後端支援此流程，確認一致。

### 中優先（影響功能）

4. **商品新增表單**：  
   選了 `CUSTOM_GACHA` 後，`subCategory` 和 `gameMode` 選項如何動態顯示？請確認 UI 互動邏輯。

5. **獎品圖片上傳**：  
   使用 S3 上傳 API（`POST /admin/upload/image`），前端上傳完取得 URL 後填入 `imageUrl` 欄位。確認此流程是否 OK？

6. **金額單位**：  
   商品 `pricePerDraw`、訂單金額等都是**金幣數**（非台幣）。台幣只有儲值報表才有。請確認 UI 標示方式（顯示「金幣」或「點」？）。

### 低優先（UI 細節）

7. **報表圖表庫**：使用哪套？ECharts / Chart.js？
8. **富文本編輯器**：新聞編輯使用哪套？Quill / TipTap / 其他？
9. **國際化（i18n）**：有多語言需求嗎？目前後端回傳全部中文訊息。

---

## 📌 前端規範建議

若前端工程師需要額外的 Vue 3 規範，可以考慮在 `/frontend/admin/` 下補充以下文件：

- `vue-skill.md` — Vue 3 Composition API 規範
- `api-layer.md` — API 封裝使用 `composables/use*.ts` 模式
- `pinia-store.md` — Store 結構（auth/store/product 各一個 store）
- `form-validation.md` — 表單驗證規範（配合後端 `@Valid`）
- `component-naming.md` — 元件命名規範

---

_最後更新：2026-04-14_  
_後端版本：Spring Boot 3.3.3｜Java 21｜Context Path: /api_
