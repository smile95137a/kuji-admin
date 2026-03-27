# Research Findings: KUJI Admin Panel Frontend

> **文件版本**: 1.0.0
> **最後更新**: 2026-02-12
> **範圍**: 後台管理介面（Admin Panel）前端技術決策與架構研究

---

## 1. 技術棧決策

### 1.1 核心框架

**Decision**: Vue 3 + Vite + Pinia + TypeScript + Vue Router 4

**Rationale**:
- Vue 3 Composition API 提升程式碼組織性，`<script setup>` 語法減少樣板程式碼
- Pinia 比 Vuex 更輕量，原生 TypeScript 支援，DevTools 整合佳
- Vite 開發伺服器冷啟動 < 300ms，HMR 幾乎即時
- TypeScript 提升大型後台系統維護性，搭配後端 DTO 定義可做型別對齊

**Alternatives considered**:
- React/Next.js：棄用，現有專案已是 Vue 生態，開發團隊熟悉度不足
- Nuxt 3：棄用，後台管理介面不需 SSR，純 SPA 即可
- Vuex 4：棄用，Pinia 已為 Vue 官方推薦狀態管理方案

**版本鎖定**:
```json
{
  "vue": "^3.4.x",
  "vite": "^5.x",
  "pinia": "^2.x",
  "vue-router": "^4.x",
  "typescript": "^5.x"
}
```

---

### 1.2 HTTP 客戶端

**Decision**: Axios（單一實例模式）

**Rationale**:
- 成熟的 interceptor 支援，可統一處理 401 refresh、error toast、token 附加
- 比 `fetch` 更易處理請求取消、逾時、重試等邊界情況
- 社群完善，TypeScript 型別定義良好

**Pattern**: 單一 axios 實例 `services/http.ts`，所有 service 模組共用

```typescript
// services/http.ts
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/admin',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

// Request Interceptor：自動附加 JWT Token
http.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

// Response Interceptor：統一錯誤處理 + 401 自動 refresh
let isRefreshing = false;
let failedQueue: Array<{ resolve: Function; reject: Function }> = [];

http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return http(originalRequest);
        });
      }
      originalRequest._retry = true;
      isRefreshing = true;
      try {
        const authStore = useAuthStore();
        const success = await authStore.refreshAccessToken();
        if (success) {
          failedQueue.forEach((p) => p.resolve(authStore.token));
          failedQueue = [];
          return http(originalRequest);
        }
      } catch {
        failedQueue.forEach((p) => p.reject(error));
        failedQueue = [];
      } finally {
        isRefreshing = false;
      }
      router.push('/login');
      return Promise.reject(error);
    }
    // 403 → 清除 token 並導向登入
    if (error.response?.status === 403) {
      const authStore = useAuthStore();
      await authStore.logout();
      router.push('/login');
    }
    return Promise.reject(error);
  }
);

export default http;
```

---

### 1.3 UI 元件庫

**Decision**: 待確認（需檢查現有 admin 專案 `package.json`）

**候選方案**:
| 元件庫 | 優點 | 缺點 |
|--------|------|------|
| Element Plus | 後台元件完整（Table、Form、Dialog）、中文文件 | 體積較大 |
| Naive UI | TypeScript 原生設計、主題客製化佳 | 社群較小 |
| Vuetify 3 | Material Design、RWD 佳 | API 風格與其他差異大 |

**重要說明**: 本文件所有 UI 描述（Table、Form、Modal、Badge、Pagination）均為**功能描述**，與元件庫無關。實作時依確認的元件庫替換對應元件名稱。

**Action needed**: 執行 `cat package.json | grep -E "element|naive|vuetify"` 確認現有依賴。

---

## 2. 認證架構決策

### 2.1 JWT Token 策略

**Decision**: JWT Bearer Token，存 localStorage

| 欄位 | 值 |
|------|-----|
| Access Token 有效期 | 24 小時 |
| Refresh Token 有效期 | 30 天 |
| 儲存位置 | localStorage（`admin_token` / `admin_refresh_token`） |
| 附加方式 | axios request interceptor 自動附加 |

**安全考量**:
- localStorage XSS 風險：後台管理介面受信任環境，可接受；生產環境建議評估 HttpOnly Cookie
- 停用帳號處理：refresh 失敗（400/401）或收到 403 → 清除所有 token → 導向 `/login`
- Token 過期前主動 refresh：App.vue `mounted` 時檢查 token 剩餘時效

### 2.2 認證流程

```
使用者輸入帳密
    ↓
POST /api/admin/user/login
    ↓
成功 → 存 token + refreshToken → fetchCurrentUser() → fetchMenus()
    ↓
失敗 → 顯示錯誤訊息（帳號密碼錯誤 / 帳號停用）
```

**Login API**: `POST /api/admin/user/login`
```json
// Request
{ "email": "admin@example.com", "password": "..." }

// Response
{
  "code": 200,
  "data": {
    "token": "eyJ...",
    "refreshToken": "eyJ...",
    "user": { "id": "...", "email": "...", "roles": ["ROLE_ADMIN"] }
  }
}
```

**Refresh API**: `POST /api/admin/user/token/refresh`
```json
// Request
{ "refreshToken": "eyJ..." }
// Response
{ "code": 200, "data": { "token": "eyJ..." } }
```

---

## 3. RBAC 側欄實作決策

### 3.1 動態選單架構

**Decision**: 從 `GET /api/admin/user/menu` 取得動態選單，存 Pinia menu store

**不在前端硬編碼選單結構的原因**:
- 不同角色（ROLE_ADMIN / ROLE_STORE_OWNER / ROLE_STORE_EDITOR）看到的選單不同
- 後端控制選單可見性，前端無需隨角色調整而修改程式碼
- 權限變更（canEdit/canDelete）即時生效，無需重新部署前端

**Router Guard 雙層保護**:
1. **第一層**（主要）：`GET /api/admin/user/menu` 回傳的菜單決定側欄顯示
2. **第二層**（防護）：`router.beforeEach` 檢查 token 有效性 + `meta.roles` 角色驗證

```typescript
// router/index.ts
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const menuStore = useMenuStore();

  // 白名單路由（登入頁不需驗證）
  if (to.meta.public) return next();

  // 未登入 → 導向登入頁
  if (!authStore.token) return next('/login');

  // 首次進入：載入使用者資訊 + 動態選單
  if (!authStore.user) {
    await authStore.fetchCurrentUser();
    await menuStore.fetchMenus();
  }

  // 角色檢查
  const requiredRoles = to.meta.roles as string[] | undefined;
  if (requiredRoles?.length && !requiredRoles.some(r => authStore.user?.roles.includes(r))) {
    return next('/403');
  }

  next();
});
```

### 3.2 選單 API 回應結構

```typescript
// GET /api/admin/user/menu 預期回應
{
  "code": 200,
  "data": [
    {
      "id": "lottery-mgmt",
      "name": "抽獎商品管理",
      "path": "/lottery",
      "icon": "lottery",
      "sort": 1,
      "parentId": null,
      "children": [
        {
          "id": "lottery-list",
          "name": "商品列表",
          "path": "/lottery/list",
          "sort": 1,
          "permissions": { "canView": true, "canEdit": true, "canDelete": false }
        }
      ]
    }
  ]
}
```

---

## 4. 查詢模式決策

### 4.1 後台 List API 統一模式

**Decision**: 所有後台列表查詢統一為 `POST .../list` + condition body

**後端規格**:
- Request: `POST /api/admin/{resource}/list` with `{ condition: {...}, sortBy, sortOrder }`
- Response: 全量資料陣列（後端不做分頁）
- 前端負責分頁顯示（`usePagination` composable）

**`useQueryList` composable 封裝**:
```typescript
// composables/useQueryList.ts
const { items, loading, condition, query, reset } = useQueryList<LotteryCondition, LotteryProduct>({
  endpoint: '/api/admin/lottery/list',
  defaultCondition: { status: 'ON_SHELF' },
  defaultSort: { sortBy: 'createdAt', sortOrder: 'DESC' }
});
```

### 4.2 搜尋與篩選模式

| 功能 | 實作方式 |
|------|---------|
| 關鍵字搜尋 | `watch(keyword, debounce(query, 300))` |
| 下拉篩選 | 直接 `query()` |
| 日期範圍 | createdAtStart / createdAtEnd |
| 重置條件 | `reset()` → 回到 defaultCondition |
| 排序 | sortBy + sortOrder，後端支援 |

---

## 5. 圖片上傳決策

### 5.1 上傳流程

**Decision**: `useFileUpload` composable，先上傳取 URL，再填入表單

```
使用者選擇圖片
    ↓
useFileUpload.upload(file)
    ↓
POST /api/upload（或 POST /api/user/avatar）
    ↓
後端存入 S3 → 回傳 { imageUrl: "https://..." }
    ↓
表單欄位 imageUrl = response.imageUrl
    ↓
提交表單時 imageUrl 作為字串傳入（不傳 base64）
```

**Action needed**: 確認 admin 後台圖片上傳的正確端點：
- 選項 A: `POST /api/upload`（通用上傳）
- 選項 B: `POST /api/admin/upload`（admin 專屬）
- 選項 C: 各資源各自的上傳端點

### 5.2 上傳限制

| 限制 | 建議值 |
|------|--------|
| 最大檔案大小 | 5MB |
| 允許格式 | image/jpeg, image/png, image/webp |
| 圖片尺寸建議 | 商品圖：600×600px；Banner：1200×400px |
| 上傳中狀態 | 顯示 loading spinner，禁用提交按鈕 |
| 錯誤處理 | 檔案過大 / 格式錯誤 → toast 錯誤訊息 |

---

## 6. 路由架構

### 6.1 路由結構規劃

```
/login                    → LoginView（public）
/                         → AdminLayout（需認證）
  /dashboard              → DashboardView
  /lottery                → LotteryLayout
    /lottery/list         → LotteryListView
    /lottery/create       → LotteryCreateView
    /lottery/:id/edit     → LotteryEditView
    /lottery/:id/prizes   → LotteryPrizesView
  /orders                 → OrderLayout
    /orders/list          → OrderListView
    /orders/:id           → OrderDetailView
  /stores                 → StoreLayout
    /stores/list          → StoreListView
    /stores/create        → StoreCreateView
    /stores/:id/edit      → StoreEditView
  /banners                → BannerListView
  /news                   → NewsLayout
    /news/list            → NewsListView
    /news/create          → NewsCreateView
    /news/:id/edit        → NewsEditView
  /users                  → UserLayout（ROLE_ADMIN 限定）
    /users/list           → UserListView
  /403                    → ForbiddenView（public）
```

### 6.2 Route Meta 規格

```typescript
interface RouteMeta {
  public?: boolean;          // true = 不需認證
  roles?: string[];          // 允許的角色，空陣列 = 任何已登入角色
  title?: string;            // 頁面標題（用於 document.title + breadcrumb）
  breadcrumb?: string[];     // 麵包屑層級
  keepAlive?: boolean;       // 是否保留元件狀態
}
```

---

## 7. 狀態管理架構

### 7.1 Pinia Store 職責劃分

| Store | 職責 | 持久化 |
|-------|------|--------|
| `auth` | token、user、login/logout/refresh | localStorage |
| `menu` | 動態選單資料、選單載入狀態 | 不持久化 |
| `ui` | sidebar 開關、global loading、toast 佇列 | 不持久化 |
| `lottery` | 抽獎商品列表快取（可選） | 不持久化 |

### 7.2 跨 Store 通訊

```typescript
// ❌ 避免循環依賴
// authStore 不應 import menuStore

// ✅ 在 router guard 中協調
router.beforeEach(async () => {
  const authStore = useAuthStore();
  const menuStore = useMenuStore();
  // 統一在此協調兩個 store 的初始化
});
```

---

## 8. 錯誤處理策略

### 8.1 全域錯誤分類

| HTTP 狀態 | 處理方式 |
|-----------|---------|
| 400 | 顯示 toast + 表單欄位錯誤（若有 fieldErrors） |
| 401 | 嘗試 refresh → 失敗則跳 /login |
| 403 | 跳 /403 頁面 |
| 404 | 顯示 toast「資源不存在」 |
| 409 | 顯示 toast（衝突，如重複商品名） |
| 422 | 顯示表單驗證錯誤 |
| 500 | 顯示 toast「伺服器錯誤，請稍後再試」 |
| 網路錯誤 | 顯示 toast「網路異常，請確認連線」 |

### 8.2 表單驗證策略

- 送出前：前端基本格式驗證（必填、長度、格式）
- 後端回傳 400 + fieldErrors：對應欄位顯示後端錯誤訊息
- 保留使用者輸入資料，不清空表單

---

## 9. API 缺口記錄（Actions Needed）

下表追蹤開發前需與後端確認的 API 不確定項目：

| 缺口 ID | 描述 | 影響功能 | 狀態 | 優先級 |
|---------|------|---------|------|--------|
| AV-001 | 圖片上傳端點（admin 後台專用或通用） | 商品/廣告/消息 | ⚠️ 需確認 | P1 |
| AV-002 | Login API 回應格式（roles 陣列格式） | 認證 | ⚠️ 需確認 | P1 |
| AV-003 | Menu API 回應：permissions 欄位是否存在 | RBAC 側欄 | ⚠️ 需確認 | P1 |
| AV-004 (011) | 批次抽獎設定欄位（batchDrawOptions）是否在商品創建 API 中 | 商品創建 | ❌ 未在 spec | P2 |
| AV-005 (011) | `weight` 欄位用途（商品排序？抽獎權重？） | 商品排序 | ⚠️ 需確認 | P2 |
| AV-006 (011) | DRAFT → CONFIGURED 狀態轉移的觸發 API | 商品生命週期 | ❌ 未說明 | P2 |
| AV-007 (011) | 自動折扣設定欄位是否在商品創建 API 中 | 商品創建 | ⚠️ 需確認 | P2 |
| AV-008 | 訂單狀態更新 API：是否支援批次操作 | 訂單管理 | ⚠️ 需確認 | P2 |
| AV-009 | Banner 顯示順序更新 API（拖拉排序？） | Banner 管理 | ⚠️ 需確認 | P3 |
| AV-010 | 店家統計資料（productCount 等）回應來源 | 店家列表 | ⚠️ 需確認 | P3 |

---

## 10. 環境設定

### 10.1 環境變數規格

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=KUJI 後台管理（開發）

# .env.production
VITE_API_BASE_URL=https://api.kuji.example.com
VITE_APP_TITLE=KUJI 後台管理
```

### 10.2 Vite 設定建議

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': '/src' }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
});
```

---

## 11. 開發規範

### 11.1 檔案結構

```
src/
├── assets/           # 靜態資源
├── components/       # 可重用元件
│   ├── common/       # 通用（Button、Table、Modal）
│   └── business/     # 業務（LotteryCard、PrizeTable）
├── composables/      # 組合式函數
│   ├── useQueryList.ts
│   ├── usePagination.ts
│   └── useFileUpload.ts
├── layouts/          # 版面元件（AdminLayout、AuthLayout）
├── router/           # 路由設定
├── services/         # API 呼叫（http.ts + 各資源 service）
├── stores/           # Pinia stores
├── types/            # TypeScript 型別定義
└── views/            # 頁面元件
```

### 11.2 命名規範

| 類型 | 規範 | 範例 |
|------|------|------|
| 元件 | PascalCase | `LotteryListView.vue` |
| Composable | camelCase + use 前綴 | `useQueryList.ts` |
| Store | camelCase + use 前綴 + Store 後綴 | `useAuthStore` |
| Service | camelCase + Service 後綴 | `lotteryService` |
| 型別/介面 | PascalCase | `LotteryProduct` |
| 常數 | UPPER_SNAKE_CASE | `ORDER_STATUS` |

---

*本文件為前端實作研究記錄，應隨開發進行持續更新。*
