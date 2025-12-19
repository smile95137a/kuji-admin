# Security & Authentication 實作總結

## 完成的修改

### 1. User Entity 與 Mapper (UUID 遷移)

**User.java** - 清理並重建為符合 DDL_UUID.sql 的結構:
- `id` 從 `Long` 改為 `String` (UUID)
- 移除所有 MySQL 權限相關欄位
- 保留: email, nickname, password, avatar, provider, providerId, goldCoins, bonusCoins, status, emailVerified, phoneNumber, lastLoginAt, createdAt, updatedAt

**UserMapper.java** - 更新方法簽名:
- `selectByPrimaryKey(String id)`
- 新增 `updateByPrimaryKeySelective(User row)`

**UserMapper.xml** - 完全重寫:
- 使用 snake_case 欄位名對應 (provider_id, gold_coins 等)
- 支援 Example 模式查詢

**UserExample.java** - 重建為正確的 Criterion 模式:
- String id 而非 Long
- 支援 provider, provider_id 查詢條件

### 2. JWT Authentication Filters

**AdminJwtAuthenticationFilter.java**:
- 加入 `@NonNull` annotations
- 改用 Example 模式查詢 AdminUser 和 AdminUserRole
- 使用 `adminUser.getId()` (已經是 String)

**ApiJwtAuthenticationFilter.java**:
- 加入 `@NonNull` annotations  
- 改用 Example 模式查詢 User
- `userId` 直接使用 `user.getId()` (String UUID)

### 3. User Provider 判斷 (EMAIL vs GOOGLE)

**UserServiceImpl.java** - 完整重寫:

```java
// 本地註冊
user.setProvider("EMAIL");

// Google OAuth 登入
user.setProvider("GOOGLE");
user.setProviderId(googleId); // Google 用戶 ID
```

登入時判斷:
```java
if (!"EMAIL".equals(user.getProvider())) {
    throw new IllegalArgumentException("Please use " + user.getProvider() + " to login");
}
```

### 4. OAuth2 Google 登入

在 `UserServiceImpl.loginWithGoogle()`:
1. 驗證 Google ID Token (呼叫 Google API)
2. 提取 email, sub (google id), picture, name
3. 如果用戶不存在 → 創建新用戶 (provider=GOOGLE)
4. 如果用戶已存在 → 更新登入時間
5. 生成 JWT Token (包含 userId, userType, roles)

### 5. JWT Token 機制

**JwtUtil.java** 功能:
- `generateToken(username, userId, userType, roles)` - 完整版 Access Token
- `generateRefreshToken(username)` - Refresh Token (30天)
- `getUserId(token)` - 返回 String UUID
- `getExpirationSeconds()` - 返回過期時間秒數

Token Claims 包含:
```json
{
  "sub": "user@email.com",
  "userId": "uuid-string",
  "userType": "user|admin",
  "roles": ["USER"]
}
```

### 6. CORS 配置

**CorsConfig.java** - 環境區分:
- `dev`: 允許所有來源 (`addAllowedOriginPattern("*")`)
- `prod`: 從 `cors.allowed-origins` 配置讀取，或使用預設域名
- 支援 credentials
- 暴露 Authorization 等 headers

### 7. 前台認證 API

**ApiAuthController.java** (`/api/auth/*`):

| 端點 | 功能 |
|------|------|
| `POST /register` | Email + 密碼註冊 (provider=EMAIL) |
| `POST /login` | Email + 密碼登入 |
| `POST /google` | Google ID Token 登入 |
| `POST /refresh` | 刷新 Access Token |

### 8. Example 模式更新

更新的 Example 類:
- **AdminUserExample.java** - 增加 `getOredCriteria()`, `getConditions()`, 欄位名改為 snake_case
- **UserExample.java** - 完全重建，使用 String id

## 配置需求

### application-dev.yml
```yaml
jwt:
  secret: your-secret-key

google:
  client-id: "your-google-client-id"

# 可選
cors:
  allowed-origins: "http://localhost:3000,http://localhost:8080"
```

### application-prod.yml
```yaml
cors:
  allowed-origins: "https://your-domain.com"
```

## API 使用範例

### 註冊
```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "John"
}
```

### 登入
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### Google 登入
```bash
POST /api/auth/google
Content-Type: application/json

{
  "idToken": "google-id-token-from-frontend"
}
```

### 回應格式
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "expiresIn": 86400,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "nickname": "John",
    "provider": "EMAIL",
    "status": "ACTIVE"
  }
}
```

## 測試

編譯成功:
```bash
mvn compile -DskipTests
# BUILD SUCCESS
```

## 待辦事項 (未來擴展)

1. **Refresh Token 存儲**: 目前 Refresh Token 僅生成，可擴展存入 `refresh_token` 表以支援撤銷
2. **Token 黑名單**: 實作 logout 時的 token 失效機制
3. **Google Client ID 驗證**: 目前僅驗證 token，可加入 aud (audience) 驗證
4. **Email 驗證**: 發送驗證郵件功能
