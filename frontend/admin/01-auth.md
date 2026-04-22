# 01 - 後台認證流程

## 登入 API

```
POST /api/admin/auth/login
Content-Type: application/json
（不需要 Authorization Header）
```

### 請求
```typescript
interface AdminLoginReq {
  username: string;  // Email
  password: string;
}
```

### 回應
```typescript
interface LoginRes {
  accessToken: string;          // 短效 JWT（約 30 分鐘）
  refreshToken: string;         // 長效刷新 Token（約 7 天）
  tokenType: 'Bearer';
  expiresIn: number;            // accessToken 有效秒數
  user: {
    id: string;
    username: string;           // Email
    displayName: string;
    roles: string[];            // 如 ["ROLE_ADMIN"] 或 ["ROLE_STORE_OWNER"]
    mustChangePassword: boolean; // true = 首次登入，必須改密碼
    storeIds: string[];         // 關聯的店家 ID 列表（ADMIN 為空陣列）
  };
  menus: MenuNode[];            // 當前使用者有權限的選單樹
}

interface MenuNode {
  id: string;
  name: string;
  path: string;           // Vue Router 路徑
  icon: string;
  orderNum: number;
  children: MenuNode[];
  permissions: {
    canView: boolean;
    canEdit: boolean;
    canDelete: boolean;
  };
}
```

### 前端流程

```
1. 呼叫 POST /admin/auth/login
2. 成功後：
   a. 儲存 accessToken 到 pinia/store（記憶體）
   b. 儲存 refreshToken 到 localStorage（持久化）
   c. 儲存 user 資訊（角色、店家 ID）
   d. 儲存 menus（動態路由/選單）
   e. 檢查 mustChangePassword：
      - true → 強制導向「首次改密碼」頁面
      - false → 正常導向 Dashboard
```

---

## 首次登入改密碼 API

Admin 建立的新帳號初始狀態為 `FIRST_LOGIN`，首次登入後**強制改密碼**才可使用系統。

```
POST /api/admin/auth/first-login/change-password
Authorization: Bearer {accessToken}
```

### 請求
```typescript
interface ChangePasswordReq {
  oldPassword: string;   // 初始密碼
  newPassword: string;   // 新密碼（至少 8 字元）
  confirmPassword: string;
}
```

### 回應
回傳新的 `LoginRes`（含新 Token），localStorage 替換刷新。

---

## 一般改密碼 API

```
POST /api/admin/auth/change-password
Authorization: Bearer {accessToken}
```

請求結構同上。

---

## Token 刷新 API

```
POST /api/admin/auth/refresh
Content-Type: application/json
（不需要 Authorization Header）
```

### 請求
```typescript
interface RefreshTokenReq {
  refreshToken: string;
}
```

### 回應
```typescript
interface RefreshRes {
  accessToken: string;
  refreshToken: string;  // 新的 refreshToken（舊的失效）
  expiresIn: number;
}
```

### 前端自動刷新邏輯

```javascript
// 建議用 Axios Interceptor 實作
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const { data } = await axios.post('/api/admin/auth/refresh', { refreshToken });
          store.setAccessToken(data.data.accessToken);
          localStorage.setItem('refreshToken', data.data.refreshToken);
          // 重試原請求
          error.config.headers['Authorization'] = `Bearer ${data.data.accessToken}`;
          return axiosInstance(error.config);
        } catch {
          // refreshToken 也過期，導向登入頁
          router.push('/login');
        }
      }
    }
    return Promise.reject(error);
  }
);
```

---

## 登出 API

```
POST /api/admin/auth/logout
Authorization: Bearer {accessToken}
```

### 前端登出流程
1. 呼叫 POST /admin/auth/logout（後端使 refreshToken 失效）
2. 清除 pinia store 中的 accessToken
3. 清除 localStorage 中的 refreshToken
4. 導向 `/login`

---

## JWT Token 結構

後端 JWT payload 包含以下欄位（前端可 decode 但勿依賴，以 API 回傳為準）：

```json
{
  "sub": "admin@kuji.com",
  "userId": "uuid-string",
  "userType": "admin",
  "roles": ["ROLE_ADMIN"],
  "exp": 1234567890,
  "iat": 1234567890
}
```

---

## 錯誤碼

| 狀態碼 | 錯誤碼 | 說明 |
|--------|--------|------|
| 401 | `INVALID_CREDENTIALS` | 帳號或密碼錯誤 |
| 403 | `ACCOUNT_INACTIVE` | 帳號已停用 |
| 400 | `MUST_CHANGE_PASSWORD` | 首次登入需改密碼 |
| 400 | `WRONG_OLD_PASSWORD` | 舊密碼錯誤 |
| 401 | `TOKEN_EXPIRED` | Token 過期 |
| 401 | `INVALID_TOKEN` | Token 無效 |
