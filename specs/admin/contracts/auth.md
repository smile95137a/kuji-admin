# API Contract: Admin Authentication

> **Base URL**: `/api/admin/auth`  
> **Auth**: 所有需驗證的端點請在 Header 加上 `Authorization: Bearer {token}`  
> **Response 格式**: `{ code: number, message: string, data: T }`

---

## TypeScript 型別定義

```typescript
// types/admin.d.ts

export interface AdminUser {
  id: string;
  email: string;
  displayName: string;
  roles: AdminRole[];
  storeId?: string;       // 店家管理員才有；超級管理員為 undefined
  status: 'ACTIVE' | 'DISABLED';
  createdAt: string;      // ISO 8601
  lastLoginAt?: string;
}

export type AdminRole =
  | 'SUPER_ADMIN'         // 超級管理員，可操作所有店家
  | 'STORE_ADMIN'         // 店家管理員，僅限自己的店家
  | 'STORE_OPERATOR';     // 店家操作員，有限權限

export interface MenuItem {
  id: string;
  name: string;           // 顯示名稱（繁體中文）
  icon?: string;          // Icon class 或 component name
  path?: string;          // Vue Router path（葉節點才有）
  children?: MenuItem[];  // 子選單
  permission?: string;    // 需要的 permission key
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
```

---

## POST /api/admin/auth/login

### Request

```typescript
interface AdminLoginReq {
  email: string;       // 管理員 Email，必填
  password: string;    // 密碼，必填，最少 8 字元
}
```

### Response (200 OK)

```typescript
interface AdminLoginRes {
  token: string;          // JWT access token（有效期 1 小時）
  refreshToken: string;   // Refresh token（有效期 7 天）
  adminUser: AdminUser;
}
```

**完整範例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "adminUser": {
      "id": "admin-001",
      "email": "admin@kuji.example.com",
      "displayName": "系統管理員",
      "roles": ["SUPER_ADMIN"],
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z",
      "lastLoginAt": "2024-06-01T08:00:00Z"
    }
  }
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 400 | 缺少必填欄位 | 顯示 field validation 錯誤 |
| 401 | email 或 password 錯誤 | Toast：「帳號或密碼錯誤，請重新輸入」 |
| 403 | 帳號已停用 | Toast：「帳號已停用，請聯絡管理員」，停用登入按鈕 |
| 429 | 短時間內嘗試登入次數過多 | Toast：「操作過於頻繁，請稍後再試」 |
| 500 | 伺服器錯誤 | Toast：「系統錯誤，請稍後再試」 |

### Frontend UI State

```typescript
// src/stores/auth.ts
const loginState = reactive({
  loading: false,
  error: null as string | null,
});

async function login(email: string, password: string) {
  loginState.loading = true;
  loginState.error = null;
  try {
    await authStore.login(email, password);
    router.push('/dashboard');
    toast.success('登入成功');
  } catch (err: any) {
    const status = err.response?.status;
    if (status === 401) loginState.error = '帳號或密碼錯誤，請重新輸入';
    else if (status === 403) loginState.error = '帳號已停用，請聯絡管理員';
    else loginState.error = '登入失敗，請稍後再試';
  } finally {
    loginState.loading = false;
  }
}
```

---

## POST /api/admin/auth/refresh

用於 access token 過期時，以 refresh token 取得新的 token 對。  
通常由 Axios interceptor 自動呼叫，無需手動觸發。

### Request

```typescript
interface RefreshTokenReq {
  refreshToken: string;
}
```

### Response (200 OK)

```typescript
interface RefreshTokenRes {
  token: string;          // 新的 JWT access token
  refreshToken: string;   // 新的 refresh token（舊的作廢）
}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 401 | refresh token 過期或無效 | 清除所有 token，導向 `/login`，Toast：「登入已過期，請重新登入」 |
| 400 | 缺少 refreshToken 欄位 | 同上 |

### Interceptor 實作

```typescript
// 在 http.ts interceptor 中處理
if (error.response?.status === 401 && !originalRequest._retry) {
  originalRequest._retry = true;
  try {
    const auth = useAuthStore();
    const ok = await auth.refreshAccessToken();
    if (ok) return http(originalRequest); // 重試原始請求
  } catch {
    // refresh 失敗
  }
  // refresh 失敗 → 登出並導向登入頁
  const auth = useAuthStore();
  await auth.logout();
  router.push('/login');
  toast.error('登入已過期，請重新登入');
}
```

---

## POST /api/admin/auth/logout

### Request

無 Body。需附加 `Authorization: Bearer {token}` Header。

### Response (200 OK)

```typescript
interface LogoutRes {
  success: true;
}
```

### 前端處理

```typescript
// 無論後端是否成功，前端都清除本地 token
async function logout() {
  try {
    await http.post('/api/admin/auth/logout');
  } catch {
    // 即使 API 呼叫失敗，仍清除本地狀態
  } finally {
    authStore.clearSession();
    router.push('/login');
  }
}
```

---

## GET /api/admin/user/me

取得目前登入的管理員資訊。通常在 App 初始化時呼叫，以確認 token 仍有效。

### Response (200 OK)

```typescript
// 回傳 AdminUser 物件（同登入 response 中的 adminUser）
interface MeRes extends AdminUser {}
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 401 | Token 無效或已過期 | Interceptor 自動處理 refresh 或登出 |

### 使用時機

```typescript
// App.vue 或 router guard 中
onMounted(async () => {
  if (authStore.token && !authStore.user) {
    try {
      await authStore.fetchMe(); // GET /api/admin/user/me
    } catch {
      authStore.clearSession();
    }
  }
});
```

---

## GET /api/admin/user/menu

取得目前管理員的可見選單（基於角色 / 權限動態產生）。  
應在登入後或 token refresh 後呼叫一次，結果存入 Pinia。

### Response (200 OK)

```typescript
interface MenuResponse {
  menus: MenuItem[];
}
```

**範例回傳：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "menus": [
      {
        "id": "store-mgmt",
        "name": "店家管理",
        "icon": "Store",
        "children": [
          { "id": "store-list", "name": "店家列表", "path": "/stores", "permission": "store:read" },
          { "id": "store-create", "name": "新增店家", "path": "/stores/new", "permission": "store:write" }
        ]
      },
      {
        "id": "product-mgmt",
        "name": "商品管理",
        "icon": "Gift",
        "children": [
          { "id": "lottery-list", "name": "抽獎商品", "path": "/products", "permission": "product:read" }
        ]
      }
    ]
  }
}
```

### Menu Store 實作

```typescript
// src/stores/menu.ts
import { defineStore } from 'pinia';
import http from '@/services/http';
import type { MenuItem } from '@/types/admin';

export const useMenuStore = defineStore('menu', {
  state: () => ({
    menus: [] as MenuItem[],
    loaded: false,
  }),
  actions: {
    async fetchMenus() {
      const res = await http.get('/api/admin/user/menu');
      this.menus = res.data.data.menus;
      this.loaded = true;
    },
    reset() {
      this.menus = [];
      this.loaded = false;
    },
  },
});
```

### Error Cases

| HTTP Code | 原因 | 前端處理 |
|-----------|------|---------|
| 401 | Token 無效 | Interceptor 自動處理 |
| 403 | 帳號沒有任何權限 | 顯示空選單或提示「帳號尚未配置權限，請聯絡管理員」 |

---

## Token 儲存策略

| 儲存位置 | Token 類型 | 優缺點 |
|---------|-----------|--------|
| `localStorage` | access + refresh token | 易於實作，但有 XSS 風險 |
| `sessionStorage` | access token | 關閉分頁後消失 |
| `httpOnly cookie` | refresh token（推薦） | 防 XSS，需後端配合 |

目前 Admin 採用 `localStorage` 儲存（開發便利性優先）。  
⚠️ 正式上線前建議與後端討論改用 `httpOnly cookie` 儲存 refresh token。

---

## 相關 Spec 文件

- `specs/admin/contracts/products.md` — 商品管理 API
- `specs/admin/contracts/stores.md` — 店家管理 API（待補）
- `specs/admin/contracts/orders.md` — 訂單管理 API（待補）
