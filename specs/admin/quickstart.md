# Quickstart: KUJI Admin Panel 前端遷移指南

本指南說明如何將 `specs/admin/` 下的規格文件遷移到實際的 Admin 前端專案，並完成初始設定。

---

## 環境需求

| 工具 | 版本 |
|------|------|
| Node.js | >= 18 |
| Vue | 3.x |
| Vite | 5.x |
| TypeScript | 5.x |
| Pinia | 2.x |
| Vue Router | 4.x |

---

## 步驟 1：確認現有 admin 專案技術棧

```bash
# 在 admin 前端專案執行
cat package.json | grep -E "(element|naive|vuetify|ant-design)"
```

根據輸出結果，更新 `specs/admin/research.md` 的 **UI Library** 欄位。  
目前推薦使用 **Element Plus** 或 **Naive UI**，兩者對 Vue 3 + TypeScript 的支援最佳。

---

## 步驟 2：遷移 spec 文件

```bash
# 複製整個 specs/admin/ 到 admin 前端專案
cp -r specs/admin/  <admin-frontend-root>/docs/specs/admin/

# 複製 CLI 合約（若需要跨系統參考）
cp -r specs/cli/contracts/  <admin-frontend-root>/docs/specs/cli/
```

建議目錄結構：

```
<admin-frontend>/
  docs/
    specs/
      admin/
        quickstart.md       ← 本文件
        research.md
        plan.md
        contracts/
          auth.md
          products.md
          ...
```

---

## 步驟 3：設定 Axios 實例

```typescript
// src/services/http.ts
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import router from '@/router';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // e.g. http://localhost:8080
  timeout: 15000,
});

// Request interceptor：附加 JWT Token
http.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

// Response interceptor：401 → 嘗試 refresh，失敗則登出
let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: unknown) => void }> = [];

http.interceptors.response.use(
  (res) => res,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // 排隊等待 refresh 完成後重試
        return new Promise<string>((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return http(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const auth = useAuthStore();
        const success = await auth.refreshAccessToken();
        if (success) {
          failedQueue.forEach((p) => p.resolve(auth.token!));
          failedQueue = [];
          isRefreshing = false;
          originalRequest.headers.Authorization = `Bearer ${auth.token}`;
          return http(originalRequest);
        }
      } catch (refreshError) {
        failedQueue.forEach((p) => p.reject(refreshError));
        failedQueue = [];
      }

      isRefreshing = false;
      const auth = useAuthStore();
      await auth.logout();
      router.push('/login');
    }

    return Promise.reject(error);
  }
);

export default http;
```

---

## 步驟 4：Router Guard

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useMenuStore } from '@/stores/menu';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/auth/LoginPage.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/DashboardPage.vue') },
        { path: 'stores', name: 'Stores', component: () => import('@/views/stores/StoreListPage.vue') },
        { path: 'products', name: 'Products', component: () => import('@/views/products/ProductListPage.vue') },
        { path: 'orders', name: 'Orders', component: () => import('@/views/orders/OrderListPage.vue') },
        { path: 'banners', name: 'Banners', component: () => import('@/views/banner/BannerListPage.vue') },
        // ... 其他路由
      ],
    },
  ],
});

router.beforeEach(async (to, _from, next) => {
  const auth = useAuthStore();
  const menu = useMenuStore();

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return next('/login');
  }

  // 已登入但選單尚未載入 → 先抓選單
  if (auth.isAuthenticated && !menu.loaded) {
    try {
      await menu.fetchMenus();
    } catch {
      // 若 fetchMenus 失敗（token 已過期），logout
      await auth.logout();
      return next('/login');
    }
  }

  next();
});

export default router;
```

---

## 步驟 5：Auth Store (Pinia)

```typescript
// src/stores/auth.ts
import { defineStore } from 'pinia';
import http from '@/services/http';
import type { AdminUser } from '@/types/admin';

interface AuthState {
  token: string | null;
  refreshToken: string | null;
  user: AdminUser | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('admin_token'),
    refreshToken: localStorage.getItem('admin_refresh_token'),
    user: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
  },
  actions: {
    async login(email: string, password: string) {
      const res = await http.post('/api/admin/auth/login', { email, password });
      const { token, refreshToken, adminUser } = res.data.data;
      this.token = token;
      this.refreshToken = refreshToken;
      this.user = adminUser;
      localStorage.setItem('admin_token', token);
      localStorage.setItem('admin_refresh_token', refreshToken);
    },
    async refreshAccessToken(): Promise<boolean> {
      if (!this.refreshToken) return false;
      try {
        const res = await http.post('/api/admin/auth/refresh', {
          refreshToken: this.refreshToken,
        });
        this.token = res.data.data.token;
        this.refreshToken = res.data.data.refreshToken;
        localStorage.setItem('admin_token', this.token!);
        localStorage.setItem('admin_refresh_token', this.refreshToken!);
        return true;
      } catch {
        return false;
      }
    },
    async logout() {
      try {
        await http.post('/api/admin/auth/logout');
      } catch { /* ignore */ }
      this.token = null;
      this.refreshToken = null;
      this.user = null;
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_refresh_token');
    },
  },
});
```

---

## 步驟 6：按 feature 建立頁面和 service

每個 spec 資料夾對應一個 feature module：

```
src/
  views/
    auth/
      LoginPage.vue
    banner/                ← 001-banner-management
      BannerListPage.vue
      BannerFormPage.vue
    orders/                ← 008-order-management
      OrderListPage.vue
      OrderDetailPage.vue
    products/              ← 011-product-lottery
      ProductListPage.vue
      ProductFormPage.vue
      ProductPrizesTab.vue
    stores/                ← 014-store-management
      StoreListPage.vue
      StoreFormPage.vue
    news/                  ← 007-news-management
    users/                 ← 009-user-management
    points/                ← 006-points-management

  services/
    bannerService.ts
    orderService.ts
    productService.ts
    storeService.ts
    newsService.ts
    userService.ts

  stores/                  ← Pinia stores
    auth.ts
    menu.ts
    product.ts
    order.ts

  components/
    common/
      AppTable.vue
      AppPagination.vue
      StatusBadge.vue
      ImageUploader.vue
    banner/
    product/
      PrizeEditor.vue
    ...

  types/
    admin.d.ts             ← AdminUser, MenuItem
    product.d.ts           ← LotteryProduct, Prize
    order.d.ts
```

Service 範例：

```typescript
// src/services/productService.ts
import http from './http';
import type { LotteryListReq, LotteryProduct, LotteryCreateReq } from '@/types/product';

export const productService = {
  list: (req: LotteryListReq) =>
    http.post<ApiResponse<LotteryProduct[]>>('/api/admin/lottery/list', req),

  getById: (id: string) =>
    http.get<ApiResponse<LotteryProduct>>(`/api/admin/lottery/${id}`),

  create: (body: LotteryCreateReq) =>
    http.post<ApiResponse<LotteryProduct>>('/api/admin/lottery', body),

  update: (id: string, body: Partial<LotteryCreateReq>) =>
    http.put<ApiResponse<LotteryProduct>>(`/api/admin/lottery/${id}`, body),

  updateStatus: (id: string, status: string) =>
    http.put(`/api/admin/lottery/${id}/status`, { status }),

  delete: (id: string) =>
    http.delete(`/api/admin/lottery/${id}`),
};
```

---

## 步驟 7：環境變數

```env
# .env.development
VITE_API_BASE_URL=http://localhost:8080
VITE_UPLOAD_URL=http://localhost:8080/api/upload

# .env.production
VITE_API_BASE_URL=https://api.kuji.example.com
VITE_UPLOAD_URL=https://api.kuji.example.com/api/upload
```

`vite.config.ts` 代理設定（開發用）：

```typescript
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

---

## 實作優先序（來自 plan.md）

| 階段 | 功能模組 | Spec 編號 | 預估天數 |
|------|----------|-----------|---------|
| Phase 1 | Auth + Layout（登入、側欄、選單） | auth | 2 |
| Phase 2 | Store 管理 | 014, 013 | 3 |
| Phase 3 | Product 管理（含獎品設定） | 011 | 4 |
| Phase 4 | Order 管理 | 008, 002 | 3 |
| Phase 5 | Content（Banner、News、Scratch） | 001, 007, 012 | 3 |
| Phase 6 | Points + RBAC | 006, 009 | 3 |

---

## 常見問題

**Q: 為何用 POST 做 list 查詢？**  
A: 後端採用 `POST /list` 模式傳遞複雜的 condition 物件，避免 URL 長度限制。

**Q: storeId 要怎麼帶？**  
A: 後端從 JWT 自動解析 storeId，前端**無需手動帶入** `storeId` 到 request body（超級管理員例外）。

**Q: 圖片上傳規格？**  
A: 見 `specs/admin/contracts/upload.md`（待補）。上傳至 `/api/upload`，回傳 `{ url: string }`。
