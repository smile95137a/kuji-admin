---
name: user-auth-flow
description: "前台會員認證流程。會員註冊、OAuth 登入、注冊淕旋、訊域常覭、金幣 vs 紅利。"
---

# 前台會員認證流程

## When to Use
- 修改前台會員登入/註冊邏輯
- 新增 Google OAuth2 登入
- 修改 Refresh Token 機制
- 處理忘記密碼/重設密碼
- 了解 provider 欄位用途

## 核心原則
- **三種註冊方式**：信箱密碼（Email+Password）、OAuth 供應商（Google 等）、重設密碼流程
- **Refresh Token 機制**：Token 分 Access Token 和 Refresh Token，分開管理生命週期
- **OAuth Provider 整合**：支持 provider 欄位標識登入來源（email/google/apple ...等）
- **密碼重設流程**：發送郵件 Token，用戶驗證後才可重設

---

## 認證架構概覽

前台走 `/api/auth/**` 路由，由 `ApiJwtAuthenticationFilter` 處理（Order 2）。

| 登入方式 | endpoint | provider 值 |
|---------|----------|------------|
| Email + 密碼 | `POST /api/auth/login` | `EMAIL` |
| Google OAuth2 | `POST /api/auth/google` | `GOOGLE` |
| 刷新 Token | `POST /api/auth/refresh` | - |

---

## user 表關鍵欄位

```java
user.getEmail()         // 唯一鍵
user.getPassword()      // 只有 provider=EMAIL 的帳號有密碼
user.getProvider()      // "EMAIL" 或 "GOOGLE"（兩者不可混用！）
user.getNickname()
user.getAvatarUrl()
user.getGoldCoins()     // 金幣（儲值獲得）
user.getBonusCoins()    // 紅利（儲值贈送/回收）
user.getIsActive()      // 1=正常, 0=停用
user.getCreatedAt()
```

---

## Email 註冊流程

```java
// UserServiceImpl.register()
@Transactional
public User register(AuthRegisterReq req) {
    // 1. 驗證密碼一致性
    if (!req.getPassword().equals(req.getConfirmPassword())) {
        throw new IllegalArgumentException("密碼與確認密碼不一致");
    }

    // 2. 檢查 Email 是否已存在（不論 provider）
    UserExample example = new UserExample();
    example.createCriteria().andEmailEqualTo(req.getEmail());
    if (!userMapper.selectByExample(example).isEmpty()) {
        throw new BusinessException("此 Email 已被註冊");
    }

    // 3. 建立帳號
    User user = new User();
    user.setId(UUID.randomUUID().toString());
    user.setEmail(req.getEmail());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setNickname(req.getNickname() != null ? req.getNickname() : req.getEmail().split("@")[0]);
    user.setProvider("EMAIL");
    user.setIsActive(1);
    user.setGoldCoins(0L);
    user.setBonusCoins(0L);
    user.setTotalRecharged(0L);
    user.setVersion(0);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    userMapper.insert(user);

    // 4. 自動處理推薦碼（如果有）
    if (req.getReferralCode() != null && !req.getReferralCode().isEmpty()) {
        referralCodeService.bindReferralCode(user.getId(), req.getReferralCode());
    }

    log.info("✅ 新用戶註冊: userId={}, email={}", user.getId(), user.getEmail());
    return user;
}
```

---

## Email 登入流程

```java
// UserServiceImpl.login()
public AuthRes login(AuthLoginReq req) {
    // 1. 查詢 user
    UserExample example = new UserExample();
    example.createCriteria().andEmailEqualTo(req.getEmail());
    List<User> users = userMapper.selectByExample(example);

    if (users.isEmpty()) {
        throw new BusinessException("帳號或密碼錯誤");
    }
    User user = users.get(0);

    // ⚠️ 關鍵：GOOGLE 帳號不允許用密碼登入
    if ("GOOGLE".equals(user.getProvider())) {
        throw new BusinessException("此帳號使用 Google 登入，請點擊 Google 登入按鈕");
    }

    // 2. 驗證密碼
    if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
        throw new BusinessException("帳號或密碼錯誤");
    }

    // 3. 檢查帳號狀態
    if (user.getIsActive() == null || user.getIsActive() != 1) {
        throw new BusinessException("帳號已被停用，請聯絡客服");
    }

    // 4. 更新最後登入時間
    user.setLastLoginAt(LocalDateTime.now());
    userMapper.updateByPrimaryKey(user);

    // 5. 生成 JWT
    return generateAuthRes(user);
}
```

---

## Google OAuth2 流程

```java
// UserServiceImpl.loginWithGoogle()
public AuthRes loginWithGoogle(AuthGoogleReq req) {
    // 1. 驗證 Google ID Token（使用 Google API）
    GoogleIdToken.Payload payload = verifyGoogleToken(req.getIdToken());
    String email = payload.getEmail();
    String googleId = payload.getSubject();

    // 2. 查找或建立帳號
    UserExample example = new UserExample();
    example.createCriteria().andEmailEqualTo(email);
    List<User> users = userMapper.selectByExample(example);

    User user;
    if (users.isEmpty()) {
        // 新用戶：自動建立
        user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setNickname((String) payload.get("name"));
        user.setAvatarUrl((String) payload.get("picture"));
        user.setProvider("GOOGLE");
        user.setGoogleId(googleId);
        user.setIsActive(1);
        user.setGoldCoins(0L);
        user.setBonusCoins(0L);
        user.setVersion(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
    } else {
        user = users.get(0);
        // ⚠️ 關鍵：EMAIL 帳號不允許用 Google 登入
        if ("EMAIL".equals(user.getProvider())) {
            throw new BusinessException("此 Email 已透過密碼方式註冊，請使用 Email 登入");
        }
    }

    return generateAuthRes(user);
}
```

---

## JWT 生成規則（前台 user）

```java
// generateAuthRes() 內部
private AuthRes generateAuthRes(User user) {
    // Access Token 攜帶 userType="user"
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("userType", "user");   // ← 前台用 "user"
    claims.put("roles", List.of("ROLE_USER"));

    String accessToken = jwtUtil.generateToken(user.getEmail(), claims, 30 * 60 * 1000L); // 30分鐘
    String refreshToken = UUID.randomUUID().toString();

    // 存儲 Refresh Token（可撤銷）
    RefreshToken rt = new RefreshToken();
    rt.setId(UUID.randomUUID().toString());
    rt.setToken(refreshToken);
    rt.setUserId(user.getId());
    rt.setUserType("user");
    rt.setExpiresAt(LocalDateTime.now().plusDays(30));
    rt.setCreatedAt(LocalDateTime.now());
    refreshTokenMapper.insert(rt);

    return new AuthRes(accessToken, refreshToken, toUserRes(user));
}
```

---

## Refresh Token 機制

```java
// POST /api/auth/refresh
public AuthRes refreshToken(String refreshToken) {
    // 1. 查詢 refresh token
    RefreshTokenExample example = new RefreshTokenExample();
    example.createCriteria().andTokenEqualTo(refreshToken);
    List<RefreshToken> tokens = refreshTokenMapper.selectByExample(example);

    if (tokens.isEmpty()) {
        throw new BusinessException("無效的 Refresh Token");
    }
    RefreshToken rt = tokens.get(0);

    // 2. 檢查是否過期
    if (rt.getExpiresAt().isBefore(LocalDateTime.now())) {
        refreshTokenMapper.deleteByPrimaryKey(rt.getId()); // 刪除過期 token
        throw new BusinessException("Refresh Token 已過期，請重新登入");
    }

    // 3. 根據 userType 查詢使用者
    if ("user".equals(rt.getUserType())) {
        User user = userMapper.selectByPrimaryKey(rt.getUserId());
        return generateAuthRes(user);
    }
    throw new BusinessException("Token 類型錯誤");
}
```

---

## 忘記密碼流程

```java
// Step 1：發送重設密碼郵件
POST /api/auth/forgot-password
Body: { "email": "user@example.com" }

// Step 2：驗證 token 並重設密碼
POST /api/auth/reset-password
Body: { "token": "uuid-reset-token", "newPassword": "new123" }
```

```java
// UserServiceImpl.forgotPassword()
public void forgotPassword(String email) {
    // 查詢使用者
    User user = findByEmail(email);
    if (user == null) return; // 不洩漏是否存在

    if ("GOOGLE".equals(user.getProvider())) {
        throw new BusinessException("Google 帳號無法使用密碼重設功能");
    }

    // 生成 reset token（有效期 1 小時）
    String resetToken = UUID.randomUUID().toString();
    user.setResetToken(resetToken);
    user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
    userMapper.updateByPrimaryKey(user);

    // 發送郵件
    emailService.sendPasswordResetEmail(email, resetToken);
}
```

---

## provider 欄位規則（關鍵！）

| provider | 密碼 | Google 登入 | 說明 |
|----------|------|------------|------|
| `EMAIL` | ✅ 有密碼 | ❌ 禁止 | 本地帳號 |
| `GOOGLE` | ❌ 無密碼 | ✅ 僅可用 | OAuth 帳號 |

> ⚠️ 同一個 Email 只能有一種 provider，兩者不可混用！

---

## SecurityUtils（前台用法）

```java
// 取得當前登入用戶 ID
String userId = SecurityUtils.getCurrentApiUserId();

// 取得當前登入用戶 Email
String email = SecurityUtils.getCurrentUsername();
```

---

## ⚠️ 禁止操作

- ❌ 不要讓 GOOGLE provider 帳號使用密碼登入
- ❌ 不要讓 EMAIL provider 帳號使用 Google 登入
- ❌ 不要在 JWT 中存密碼
- ❌ 不要在找不到 Email 時拋出「Email 不存在」（安全性：防止枚舉攻擊）
- ❌ 忘記密碼發送時，無論 Email 是否存在都回傳成功（不洩漏資訊）
- ❌ 不要在沒有 @Transactional 的情況下同時寫入 user 和 referral
