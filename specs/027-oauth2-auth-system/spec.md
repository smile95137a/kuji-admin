# Feature Specification: OAuth2 多 Provider 認證系統

**Feature Branch**: `027-oauth2-auth-system`  
**Created**: 2026-04-22  
**Status**: Implemented  
**前置依賴**: Feature 024（帳號安全強化）已完成  

---

## 背景與目標

### 現況問題
`OAuth2Controller.java` 目前只是一個 **stub**，Google 授權回來後僅回傳 email/name，沒有：
- 寫入或查詢 `user` 資料表
- 產生 JWT Token
- 任何帳號衝突防護

### 目標
1. 完整實作 Google OAuth2 登入 / 自動註冊流程
2. 建立可擴充的 Provider 架構（日後加 Apple / LINE 不需改 DB 結構）
3. 明確的帳號衝突防護，防止 Local 帳號與 OAuth 帳號混用
4. 補足 Google 拿不到的資料（電話）採選填策略

---

## 技術決策

| 決策點 | 選擇 | 理由 |
|--------|------|------|
| OAuth 流程 | **前端主導（A 方案）** | 前端取得 Google ID Token 後 POST 給後端，後端驗證並簽發 JWT；適合 SPA 架構 |
| 電話號碼 | **選填** | Google 無法提供 phone；用戶可事後在個人資料頁補填 |
| 帳號衝突 | **硬擋並提示** | 同 Email 只能有一種 provider，衝突時顯示「請改用 X 方式登入」 |
| Provider 擴充 | **資料層已保留彈性** | `provider` 欄位字串化（EMAIL / GOOGLE / APPLE / LINE），加新 provider 不需 migration |

---

## 資料模型

### `user` 表（現有欄位，確認已到位）

| 欄位 | 型別 | 說明 |
|------|------|------|
| `provider` | VARCHAR(20) | `EMAIL`、`GOOGLE`、`APPLE`、`LINE`（預留） |
| `provider_id` | VARCHAR(255) | OAuth provider 的 unique user ID（Google `sub`）|
| `email` | VARCHAR(255) UNIQUE | 唯一鍵，跨 provider 共用 |
| `password` | VARCHAR(255) NULL | 只有 `EMAIL` 帳號有值 |
| `avatar` | TEXT NULL | 可從 Google `picture` 帶入 |
| `email_verified` | TINYINT(1) | Google 帳號預設為 1（已驗證） |

> ⚠️ **不需要 migration**：`provider` 和 `provider_id` 欄位已存在於 `user` 表。

---

## Google OAuth2 可取得資料

| Google 屬性 | 說明 | 對應 `user` 欄位 |
|------------|------|----------------|
| `sub` | Google 唯一 user ID | `provider_id` |
| `email` | 電子信箱 | `email` |
| `email_verified` | 是否已驗證 | `email_verified`（設為 1）|
| `name` | 全名 | `nickname`（初始值）|
| `picture` | 大頭貼 URL | `avatar` |
| `given_name` | 名 | 暫不存，可日後擴充 |
| `family_name` | 姓 | 暫不存，可日後擴充 |
| `locale` | 語系 | 不存 |
| 電話號碼 | — | ❌ Google 不提供，選填補 |
| 生日 | — | ❌ Google 不提供 |

---

## API 設計

### 1. Google OAuth2 登入 / 自動註冊

```
POST /api/auth/oauth2/google
```

**Request Body**（前端拿到 Google ID Token 後呼叫）：
```json
{
  "idToken": "eyJhbGciOiJSUzI1Ni..."
}
```

**成功 Response**（與 local 登入格式一致）：
```json
{
  "success": true,
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "isNewUser": true,
    "user": {
      "id": "uuid",
      "email": "user@gmail.com",
      "nickname": "王小明",
      "avatar": "https://...",
      "provider": "GOOGLE",
      "phoneNumber": null
    }
  }
}
```

> `isNewUser: true` → 前端可顯示「歡迎加入！」引導用戶補填資料。

**錯誤情境**：

| HTTP | errorCode | 說明 |
|------|-----------|------|
| 400 | `INVALID_GOOGLE_TOKEN` | Google ID Token 驗證失敗 |
| 409 | `EMAIL_PROVIDER_CONFLICT` | 此 Email 已用 local 方式註冊 |
| 403 | `ACCOUNT_DISABLED` | 帳號被停用 |

---

### 2. 現有登入補強（衝突防護）

`POST /api/auth/login`（Email/密碼登入）現有邏輯需補上：

```
若 user.provider != 'EMAIL' → 回傳 EMAIL_PROVIDER_CONFLICT
錯誤訊息：「此帳號使用 Google 登入，請點擊 Google 登入按鈕」
```

---

### 3. OAuth Provider 狀態查詢（選用，供前端 UI 判斷）

```
GET /api/user/me/provider
```

Response：
```json
{
  "provider": "GOOGLE",
  "canSetPassword": false
}
```

> `canSetPassword: false` 表示 OAuth 帳號，前端隱藏「修改密碼」入口。

---

## 核心業務邏輯

### Google 登入流程圖

```
前端取得 Google ID Token
        ↓
POST /api/auth/oauth2/google { idToken }
        ↓
後端向 Google 驗證 ID Token（Google Auth Library）
        ↓ 驗證失敗 → 400 INVALID_GOOGLE_TOKEN
        ↓ 驗證成功
取得 email, sub, name, picture, email_verified
        ↓
查詢 user 表（by email）
        ↓
  [找到且 provider=EMAIL] → 409 EMAIL_PROVIDER_CONFLICT
  [找到且 provider=GOOGLE] → 更新 last_login_at，簽發 JWT
  [找到且 provider=其他] → 409 EMAIL_PROVIDER_CONFLICT
  [找不到] → 自動建立新帳號（provider=GOOGLE）+ 簽發 JWT
```

### 帳號衝突規則（雙向）

| 情境 | 結果 |
|------|------|
| Email 已用 `EMAIL` 方式註冊，再用 Google 登入 | 409，提示：「此 Email 已用 Email/密碼方式註冊，請改用密碼登入」 |
| Email 已用 `GOOGLE` 方式註冊，再用密碼登入 | 401，提示：「此帳號使用 Google 登入，請點擊 Google 登入按鈕」 |
| Email 已用 `GOOGLE` 方式註冊，再用 Email 表單嘗試「註冊」 | 409，提示：「此 Email 已存在，請直接登入」 |

> **原則**：同一個 Email 在系統中只能對應一種 provider，永遠不合併帳號。

### 新帳號自動建立邏輯

```java
User newUser = new User();
newUser.setId(UUID.randomUUID().toString());
newUser.setEmail(payload.getEmail());
newUser.setNickname(payload.get("name"));       // Google name
newUser.setAvatar(payload.get("picture"));       // Google picture
newUser.setProvider("GOOGLE");
newUser.setProviderId(payload.getSubject());     // Google sub
newUser.setEmailVerified((byte) 1);              // Google 已驗證
newUser.setPassword(null);                       // OAuth 帳號無密碼
newUser.setStatus("ACTIVE");
newUser.setGoldCoins(0L);
newUser.setBonusCoins(0L);
newUser.setTotalRecharged(0L);
newUser.setVersion(0);
newUser.setCreatedAt(LocalDateTime.now());
newUser.setUpdatedAt(LocalDateTime.now());
```

---

## Provider 擴充機制（未來）

### 設計原則
- `provider` 欄位為字串，加新 provider 不需改 DB
- 每個 provider 實作 `OAuthProviderHandler` 介面

```java
// 介面設計（預留）
public interface OAuthProviderHandler {
    String getProviderName();          // "GOOGLE", "APPLE", "LINE"
    OAuthUserInfo verify(String token); // 驗證 token 並取得用戶資料
}

// OAuthUserInfo 通用結構
public class OAuthUserInfo {
    String providerId;   // 各 provider 的 unique ID
    String email;
    String nickname;
    String avatarUrl;
    boolean emailVerified;
}
```

未來加 Apple：新增 `AppleOAuthProviderHandler implements OAuthProviderHandler`，再加一個 endpoint `/api/auth/oauth2/apple` 即可，不影響現有邏輯。

---

## 實作範圍

### Backend

- [ ] `GoogleOAuthService.java`：驗證 Google ID Token（使用 `google-api-client` library）
- [ ] `UserAuthService.java`（或現有 service）：新增 `loginWithGoogle()` 方法
  - 查詢 email → 衝突檢查 → 自動建立或取得帳號 → 簽發 JWT
- [ ] `OAuth2Controller.java`：改寫現有 stub，實作 `POST /api/auth/oauth2/google`
- [ ] 在 `login()` 方法補上 provider 衝突檢查
- [ ] `SecurityConfig`：確認 `/api/auth/oauth2/**` 為公開路徑

### Frontend（後續 spec）
- 前端串接 Google Sign-In SDK（`@react-oauth/google` 或 `vue3-google-oauth2`）
- idToken 取得後 POST 給後端
- `isNewUser=true` 時顯示歡迎提示，引導至個人資料頁補電話

---

## 不實作的項目（本期）

- ❌ Apple / LINE / Facebook OAuth（預留架構，不實作）
- ❌ 帳號合併（Account Linking）- 帳號衝突一律擋住，不合併
- ❌ 強制補填電話 — 電話為選填，由個人資料頁完成
- ❌ Server-side Spring Security redirect flow — 採前端主導 A 方案

---

## 安全考量

| 威脅 | 防護措施 |
|------|---------|
| 偽造 idToken | 後端必須向 Google 官方 endpoint 驗證，不信任前端解碼的 claims |
| 枚舉帳號是否存在 | 衝突錯誤訊息不洩漏「帳號存在」，只說「此 Email 已用其他方式註冊」|
| OAuth 帳號被停用 | check `user.status = ACTIVE` 才簽發 JWT |
| provider_id 重複 | `provider_id` 在 DB 加 UNIQUE INDEX（`provider`, `provider_id`）|

---

## 架構澄清

### JWT Token 一致性
不管是 Local 或 Google 登入，JWT 都由**後端統一簽發**，結構完全相同：
```json
{ "sub": "email", "userId": "uuid", "userType": "user", "roles": ["ROLE_USER"] }
```
前端拿到 token 後不需知道登入方式，所有 API 行為一致。

### 帳號停用
兩種登入路徑都必須 check `user.status = 'ACTIVE'`，OAuth 路徑不是例外。

### `provider_id` 的用途
`provider_id` 存 Google 的 `sub`（每個 Google 帳號的唯一永久 ID）：
- **主要用途**：防呆 — 確保同一個 Google 帳號不會綁定兩個不同系統帳號
- **次要用途**：未來若用戶換了 Gmail，仍能透過 sub 識別同一人（進階功能，本期不實作）
- 目前查詢邏輯**以 email 為主**，`provider_id` 是資料庫層的額外防護

### 前台 vs 後台明確分界
| | 前台（`/api/**`）| 後台（`/admin/**`）|
|--|--|--|
| 登入方式 | Email/密碼 **或** Google OAuth | **只能** Email/密碼 |
| 資料表 | `user` | `admin_user` |
| OAuth 支援 | ✅ | ❌ 永不支援 |

本 spec 所有討論均**僅針對前台用戶**，後台不受影響。

---

## 決策記錄（已確認）

| 項目 | 決策 |
|------|------|
| Google ID Token 驗證 SDK | `com.google.api-client:google-api-client` |
| Refresh Token 策略 | 共用現有 `refresh_token` 表，`user_type='user'`，沿用 024 設計 |
| Nickname 更新策略 | **不覆蓋** — 只有首次建立帳號時帶入 Google name，之後用戶自行修改不受 Google 登入影響 |
