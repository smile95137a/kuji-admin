# 合約修改：POST /api/auth/register（新增推薦碼支援）

**官網 Email 註冊時支援推薦碼綁定**

---

## 修改摘要

原有 `/api/auth/register` 端點新增可選的 `referralCode` 欄位。

---

## HTTP 請求

### 請求路徑

```
POST /api/auth/register
Content-Type: application/json
```

### 請求體（新增欄位）

```json
{
  "email": "user@example.com",
  "password": "secure123456",
  "confirmPassword": "secure123456",
  "nickname": "NewUser",
  "referralCode": "STORE-ABC123"
}
```

#### 欄位說明

| 欄位名 | 型別 | 需求 | 說明 |
|-------|------|------|------|
| `email` | String | ✓ 必填 | 使用者 Email（唯一） |
| `password` | String | ✓ 必填 | 密碼（≥ 8 字元） |
| `confirmPassword` | String | ✓ 必填 | 確認密碼 |
| `nickname` | String | ✓ 必填 | 昵稱 |
| `referralCode` | String | ✗ 可選 | 推薦碼（1-50 字元）**← 新增** |

---

## HTTP 回應

### 200 OK — 註冊成功（有推薦碼）

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 3600,
    "user": {
      "id": "user-uuid",
      "email": "user@example.com",
      "nickname": "NewUser",
      "avatar": null,
      "goldCoins": 0,
      "bonusCoins": 0,
      "referralCode": "STORE-ABC123",
      "referredStoreId": "store-uuid",
      "isNewUser": false,
      "createdAt": "2026-04-14T10:30:00Z"
    }
  }
}
```

### 200 OK — 註冊成功（無推薦碼）

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "...",
    "refreshToken": "...",
    "expiresIn": 3600,
    "user": {
      "id": "user-uuid",
      "email": "user@example.com",
      "nickname": "NewUser",
      "referralCode": null,
      "referredStoreId": null,
      "isNewUser": false,
      "createdAt": "2026-04-14T10:30:00Z"
    }
  }
}
```

### 400 Bad Request — 推薦碼無效

```json
{
  "code": 400,
  "message": "推薦碼無效或已停用",
  "error": "INVALID_REFERRAL_CODE"
}
```

### 400 Bad Request — Email 已存在

```json
{
  "code": 400,
  "message": "此 Email 已被註冊",
  "error": "EMAIL_ALREADY_EXISTS"
}
```

### 400 Bad Request — 密碼不一致

```json
{
  "code": 400,
  "message": "密碼與確認密碼不一致",
  "error": "PASSWORD_MISMATCH"
}
```

---

## 驗證規則

### 推薦碼驗證邏輯

**若使用者提供 `referralCode`**：

1. ✓ 推薦碼存在
2. ✓ 推薦碼活躍（not disabled）
3. ✓ 推薦碼未過期
4. ✓ 推薦碼未超限（usedCount < maxUsage）
5. ✓ 推薦來源店家活躍（store.status = ACTIVE）

**任一條件不滿足 → 回傳 400 Bad Request**

**若不提供 `referralCode` 或為空 → 正常完成註冊（`user.referralCode = null`）**

---

## 後端邏輯

### 修改 UserService.register()

```java
@Transactional
public User register(AuthRegisterReq req) {
    // 1. 驗證密碼一致性
    if (!req.getPassword().equals(req.getConfirmPassword())) {
        throw new BusinessException("密碼與確認密碼不一致");
    }
    
    // 2. 檢查 Email 唯一性
    if (userMapper.selectByEmail(req.getEmail()) != null) {
        throw new BusinessException("此 Email 已被註冊");
    }
    
    // 3. 驗證推薦碼（若有）
    String referralCode = null;
    String referredStoreId = null;
    if (req.getReferralCode() != null && !req.getReferralCode().trim().isEmpty()) {
        ReferralCode validCode = referralCodeService.validateAndGetReferralCode(
            req.getReferralCode().trim().toUpperCase());
        
        if (validCode == null) {
            throw new BusinessException("推薦碼無效或已停用");
        }
        referralCode = req.getReferralCode().toUpperCase();
        referredStoreId = validCode.getStoreId();
    }
    
    // 4. 建立用戶
    User user = new User();
    user.setId(UUID.randomUUID().toString());
    user.setEmail(req.getEmail());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setNickname(req.getNickname());
    user.setProvider("EMAIL");
    user.setIsActive((byte) 1);
    user.setReferralCode(referralCode);
    user.setReferredStoreId(referredStoreId);
    if (referralCode != null) {
        user.setReferralBoundAt(LocalDateTime.now());
    }
    user.setIsOauthNewUser(0);
    user.setCreatedAt(LocalDateTime.now());
    
    userMapper.insert(user);
    
    // 5. 建立推薦紀錄
    if (referralCode != null) {
        referralCodeService.createReferralRecord(user, referralCode, "EMAIL");
        referralCodeMapper.incrementUsageCount(...);
    }
    
    return user;
}
```

---

## 使用範例

### 前端：註冊表單

```javascript
async function handleRegister(formData) {
  const payload = {
    email: formData.email,
    password: formData.password,
    confirmPassword: formData.confirmPassword,
    nickname: formData.nickname,
    referralCode: formData.referralCode || null  // 可選
  };
  
  try {
    const response = await axios.post('/api/auth/register', payload);
    const { accessToken, user } = response.data.data;
    
    localStorage.setItem('accessToken', accessToken);
    
    if (user.referralCode) {
      showToast(`感謝推薦！推薦店家：${user.referredStoreName}`);
    }
    
    router.push('/home');
  } catch (error) {
    showError(error.response.data.message);
  }
}
```

### 前端：推薦碼即時驗證

```javascript
async function validateReferralAtInput(code) {
  if (!code || code.length === 0) {
    clearReferralInfo();
    return;
  }
  
  const response = await axios.post('/api/auth/validate-referral', { code });
  
  if (response.data.data.valid) {
    showReferralInfo(`推薦店家：${response.data.data.storeName}`);
  } else {
    showError(`推薦碼無效：${response.data.data.reason}`);
  }
}
```

---

## 相關端點

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/auth/validate-referral` | 先行驗證推薦碼（無需登入） |
| POST | `/api/user/apply-referral` | 登入後補上推薦碼 |
| GET | `/api/user/me` | 查詢個人資訊（含 referralCode） |

