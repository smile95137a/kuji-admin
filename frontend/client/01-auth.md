# 01 - 認證流程（Auth）

> **路由前綴**：`/auth`  
> **均為公開端點（不需要 Token）**

---

## API 列表

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/auth/register` | Email 密碼註冊 |
| POST | `/auth/login` | Email 密碼登入 |
| POST | `/auth/google` | Google OAuth 登入 |
| POST | `/auth/refresh` | 刷新 Access Token |
| POST | `/auth/forgot-password` | 忘記密碼（發送重設信） |
| POST | `/auth/reset-password` | 重設密碼 |

---

## 回應類型（AuthRes）

```typescript
interface AuthRes {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;        // accessToken 有效秒數
  tokenType: 'Bearer';
  user: {
    id: string;
    email: string;
    nickname: string;
    avatarUrl: string | null;
    provider: 'EMAIL' | 'GOOGLE';
    status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
    goldCoins: number;       // 金幣餘額
    bonusCoins: number;      // 紅利餘額
    referralCode: string | null;
    createdAt: string;
  };
}
```

---

## Email 註冊

```
POST /api/auth/register
（無需 Authorization）
```

### 請求
```typescript
interface AuthRegisterReq {
  email: string;            // 必填，格式驗證
  password: string;         // 必填，至少 8 字元
  confirmPassword: string;  // 必填，需與 password 一致
  nickname?: string;        // 可選
  referralCode?: string;    // 推薦碼（可選，填後雙方獲得獎勵）
}
```

### 業務規則
- Email 已存在 → `409`
- `password !== confirmPassword` → `400`
- 註冊成功後**直接回傳 Token**（不需要另外登入）
- 若有填 `referralCode`，自動套用（需在第一次完成首次操作後觸發獎勵）

---

## Email 登入

```
POST /api/auth/login
（無需 Authorization）
```

### 請求
```typescript
interface AuthLoginReq {
  email: string;
  password: string;
}
```

### 業務規則
- 帳號不存在 → `401`
- 密碼錯誤 → `401`
- 帳號 `INACTIVE`/`SUSPENDED` → `403`
- Google 帳號（`provider=GOOGLE`）不可用 Email 密碼登入 → `400`

---

## Google OAuth 登入

```
POST /api/auth/google
（無需 Authorization）
```

### 請求
```typescript
interface AuthGoogleReq {
  idToken: string;   // 前端從 Google SDK 取得的 ID Token
}
```

### 流程說明
```
1. 前端使用 Google OAuth SDK 取得 idToken
2. 傳送 idToken 給後端
3. 後端驗證 idToken（向 Google 確認）
4. 若 Email 不存在 → 自動建立帳號（provider=GOOGLE）
5. 若 Email 已存在且 provider=EMAIL → 400（帳號衝突）
6. 成功回傳 AuthRes（與 Email 登入相同格式）
```

### 前端 Google Sign-In 範例
```javascript
import { GoogleLogin } from '@react-oauth/google'  // Vue 版本使用 vue3-google-login

// 取得 idToken 後傳給後端
const handleGoogleSuccess = async (credentialResponse) => {
  const { data } = await api.post('/api/auth/google', {
    idToken: credentialResponse.credential
  })
  authStore.setToken(data.data.accessToken)
  localStorage.setItem('refreshToken', data.data.refreshToken)
}
```

---

## Token 刷新

```
POST /api/auth/refresh
（無需 Authorization）
```

### 請求
```typescript
interface RefreshTokenReq {
  refreshToken: string;
}
```

### 回應：完整 AuthRes（含新 accessToken 和 refreshToken）

### ⚠️ 重要
- 每次刷新後，舊的 refreshToken **立即失效**
- 新的 refreshToken **必須存回 localStorage**
- 如果 refreshToken 也過期（401），導向登入頁

---

## 忘記密碼

```
POST /api/auth/forgot-password
```

### 請求
```typescript
interface ForgotPasswordReq {
  email: string;
}
```

後端發送重設密碼連結到 Email（含 Token），成功回傳 200（無論 Email 是否存在，避免用戶枚舉）。

---

## 重設密碼

```
POST /api/auth/reset-password
```

### 請求
```typescript
interface ResetPasswordReq {
  token: string;        // 從 Email 連結取得的 Token
  newPassword: string;
  confirmPassword: string;
}
```

---

## Token 儲存建議

```javascript
// Pinia Store (記憶體，頁面關閉後消失)
const authStore = {
  accessToken: '',   // 短效（約 30 分鐘）
  user: null
}

// localStorage（持久化）
localStorage.setItem('refreshToken', refreshToken)

// 頁面刷新後自動恢復
// App.vue 的 onMounted 中：
const refreshToken = localStorage.getItem('refreshToken')
if (refreshToken) {
  try {
    const { data } = await api.post('/api/auth/refresh', { refreshToken })
    authStore.setToken(data.data.accessToken)
    authStore.setUser(data.data.user)
    localStorage.setItem('refreshToken', data.data.refreshToken)
  } catch {
    localStorage.removeItem('refreshToken')
    // 靜默失敗，讓使用者繼續匿名瀏覽
  }
}
```

---

## 錯誤碼說明

| 狀態碼 | 場景 |
|--------|------|
| `400` | 密碼確認不一致、Google 帳號嘗試用 Email 登入 |
| `401` | 帳號或密碼錯誤、Token 無效或過期 |
| `403` | 帳號被停用（`INACTIVE`/`SUSPENDED`） |
| `409` | Email 已被註冊 |
