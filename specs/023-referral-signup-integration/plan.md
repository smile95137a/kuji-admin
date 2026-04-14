# 實作計劃：推薦碼於使用者註冊流程整合

**功能分支**：`023-referral-signup-integration`  
**計劃日期**：2026-04-14  
**預估工期**：2-3 個工作日  

---

## 階段分劇

### 第 1 階段：資料模型 & DB 遷移（準備）

**預估**：2-4 小時

#### 1.1 新增 user 表欄位

```sql
ALTER TABLE user ADD COLUMN referral_code VARCHAR(50) 
  DEFAULT NULL UNIQUE COMMENT '推薦碼（一次性）';

ALTER TABLE user ADD COLUMN referred_store_id VARCHAR(36) 
  DEFAULT NULL COMMENT '推薦來源店家 ID';

ALTER TABLE user ADD COLUMN referral_bound_at TIMESTAMP 
  DEFAULT NULL COMMENT '推薦碼綁定時間';

ALTER TABLE user ADD COLUMN is_oauth_new_user TINYINT(1) 
  DEFAULT 0 COMMENT 'OAuth 新用戶標記';

-- 加 FK 約束
ALTER TABLE user ADD CONSTRAINT fk_user_referred_store
  FOREIGN KEY (referred_store_id) REFERENCES store(id);
```

#### 1.2 新增 referral_record 欄位

```sql
ALTER TABLE referral_record ADD COLUMN signup_method ENUM('EMAIL', 'OAUTH') 
  DEFAULT 'EMAIL' COMMENT 'EMAIL=官網註冊, OAUTH=登入後補碼';
```

#### 1.3 執行 MyBatis Generator

```bash
mvn mybatis-generator:generate
```

重新生成 `User.java`, `UserExample.java`, `UserMapper.java`, 以及 `UserMapper.xml`

#### 1.4 建立 DB 遷移指令碼

檔案：`sql/V_2026_04_14__add_referral_signup_fields.sql`

---

### 第 2 階段：後端 API 層（核心邏輯）

**預估**：1 天

#### 2.1 擴充 AuthRegisterReq DTO

`src/main/java/com/group/admin/req/AuthRegisterReq.java`

```java
@Data
@NoArgsConstructor
public class AuthRegisterReq {
    @Email
    private String email;
    
    @Size(min = 8)
    private String password;
    
    @Size(min = 8)
    private String confirmPassword;
    
    private String nickname;
    
    // ← NEW: 推薦碼欄位
    @Size(max = 50, message = "推薦碼長度不超過 50 字元")
    private String referralCode;  // 可選
}
```

#### 2.2 新增公開驗證端點

建立：`src/main/java/com/group/admin/controller/api/ReferralValidationController.java`

```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ReferralValidationController {
    
    private final ReferralCodeService referralCodeService;
    
    /**
     * POST /api/auth/validate-referral
     * 公開端點：驗證推薦碼有效性（用於註冊表單）
     */
    @PostMapping("/validate-referral")
    public ResponseEntity<ApiResponse<ReferralValidationRes>> validateReferral(
            @Valid @RequestBody ReferralValidationReq req) {
        // ...實現見 2.5
    }
}
```

#### 2.3 新增已認證用戶補碼端點

修改：`src/main/java/com/group/admin/controller/api/UserController.java`

```java
@PostMapping("/apply-referral")
@Operation(summary = "補上推薦碼", description = "OAuth 新用戶登入後補上推薦碼（一次性）")
public ResponseEntity<Void> applyReferral(
        @Valid @RequestBody ApplyReferralReq req) {
    String userId = SecurityUtils.getCurrentUserId();
    referralCodeService.applyReferralForUser(userId, req.getCode());
    return ResponseEntity.ok().build();
}
```

#### 2.4 修改 UserService.register()

整合推薦碼綁定邏輯

```java
@Transactional
public User register(AuthRegisterReq req) {
    // 1. 驗證密碼一致性
    if (!req.getPassword().equals(req.getConfirmPassword())) {
        throw new BusinessException("密碼與確認密碼不一致");
    }
    
    // 2. 檢查 Email 唯一性
    User existing = userMapper.selectByEmail(req.getEmail());
    if (existing != null) {
        throw new BusinessException("此 Email 已被註冊");
    }
    
    // 3. 驗證推薦碼（若有）
    String referralCode = null;
    String referredStoreId = null;
    if (req.getReferralCode() != null && !req.getReferralCode().isEmpty()) {
        ReferralCode validCode = validateAndGetReferralCode(req.getReferralCode());
        if (validCode == null) {
            throw new BusinessException("推薦碼無效或已停用");
        }
        referralCode = req.getReferralCode();
        referredStoreId = validCode.getStoreId();
    }
    
    // 4. 建立用戶
    User user = new User();
    user.setId(UUID.randomUUID().toString());
    user.setEmail(req.getEmail());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setNickname(req.getNickname());
    user.setProvider("EMAIL");
    user.setReferralCode(referralCode);
    user.setReferredStoreId(referredStoreId);
    if (referralCode != null) {
        user.setReferralBoundAt(LocalDateTime.now());
    }
    user.setIsOauthNewUser(0);  // EMAIL 註冊為 0
    userMapper.insert(user);
    
    // 5. 建立推薦紀錄（若有）
    if (referralCode != null) {
        createReferralRecord(user, referralCode, "EMAIL");
    }
    
    return user;
}
```

#### 2.5 實作推薦碼服務方法

修改：`src/main/java/com/group/admin/service/impl/ReferralCodeServiceImpl.java`

```java
/**
 * 驗證推薦碼並取得對象（用於註冊）
 */
public ReferralCode validateAndGetReferralCode(String code) {
    ReferralCode rc = referralCodeMapper.selectByCode(code.toUpperCase());
    if (rc == null || !Boolean.TRUE.equals(rc.getIsActive())) {
        return null;
    }
    if (rc.getMaxUsage() != null && rc.getUsedCount() >= rc.getMaxUsage()) {
        return null;  // 已超限
    }
    if (rc.getExpiresAt() != null && rc.getExpiresAt().isBefore(LocalDateTime.now())) {
        return null;  // 已過期
    }
    Store store = storeMapper.selectByPrimaryKey(rc.getStoreId());
    if (store == null || !"ACTIVE".equals(store.getStatus())) {
        return null;  // 店家不活躍
    }
    return rc;
}

/**
 * 為 OAuth 新用戶補上推薦碼（一次性）
 */
@Transactional
public void applyReferralForUser(String userId, String code) {
    User user = userMapper.selectByPrimaryKey(userId);
    if (user == null) {
        throw new BusinessException("用戶不存在");
    }
    
    // 檢查是否已綁定
    if (user.getReferralCode() != null) {
        throw new BusinessException("推薦碼已綁定，無法修改");
    }
    
    // 驗證推薦碼
    ReferralCode rc = validateAndGetReferralCode(code);
    if (rc == null) {
        throw new BusinessException("推薦碼無效或已停用");
    }
    
    // 防自推薦（比對店家負責人帳號）
    // ...業務邏輯根據需求調整
    
    // 綁定推薦碼
    user.setReferralCode(code.toUpperCase());
    user.setReferredStoreId(rc.getStoreId());
    user.setReferralBoundAt(LocalDateTime.now());
    userMapper.updateByPrimaryKey(user);
    
    // 建立推薦紀錄
    createReferralRecord(user, code, "OAUTH");
    
    // 增加推薦碼使用計數
    referralCodeMapper.incrementUsageCount(rc.getId());
}

/**
 * 建立推薦紀錄
 */
private void createReferralRecord(User user, String code, String signupMethod) {
    ReferralRecord record = new ReferralRecord();
    record.setId(UUID.randomUUID().toString());
    record.setUserId(user.getId());
    record.setReferralCode(code.toUpperCase());
    record.setStoreId(user.getReferredStoreId());
    record.setSignupMethod(signupMethod);  // EMAIL 或 OAUTH
    record.setCreatedAt(LocalDateTime.now());
    referralRecordMapper.insert(record);
}
```

#### 2.6 整合 Google OAuth 流程

修改：`src/main/java/com/group/admin/service/impl/UserServiceImpl.java`

在 `loginWithGoogle()` 中，新用戶建立時設置 `is_oauth_new_user = 1`

```java
public AuthRes loginWithGoogle(AuthGoogleReq req) {
    // ... 驗證 Google ID Token 邏輯
    
    String email = googlePayload.getEmail();
    User existingUser = userMapper.selectByEmail(email);
    
    if (existingUser == null) {
        // 新用戶：建立帳號，標記 OAuth 新用戶
        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setEmail(email);
        newUser.setProvider("GOOGLE");
        newUser.setNickname(googlePayload.getName());
        newUser.setAvatar(googlePayload.getPicture());
        newUser.setIsOauthNewUser(1);  // ← 標記
        userMapper.insert(newUser);
        
        // 回傳 token + 新用戶標記
        String accessToken = jwtUtil.generateToken(email, newUser.getId(), "user", List.of("ROLE_USER"));
        return AuthRes.builder()
            .accessToken(accessToken)
            .refreshToken(jwtUtil.generateRefreshToken(email))
            .isNewUser(true)  // ← 前端用此判斷是否進導覽
            .build();
    } else {
        // 既存用戶：直接登入
        return login(new AuthLoginReq(email, ""));  // 不驗證密碼
    }
}
```

#### 2.7 修改 UserServiceImpl & UserController

新增 `GET /api/user/me` 回傳 `isNewUser` 欄位

```java
public UserRes me(String userId) {
    User user = userMapper.selectByPrimaryKey(userId);
    return UserRes.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .goldCoins(user.getGoldCoins())
        .bonusCoins(user.getBonusCoins())
        .referralCode(user.getReferralCode())
        .isNewUser(user.getIsOauthNewUser() == 1)  // ← NEW
        .build();
}
```

---

### 第 3 階段：安全性防護

**預估**：4-6 小時

#### 3.1 資料庫限制

設定 `referral_code` 欄位為 UNIQUE（確保一碼一人）

```sql
ALTER TABLE user ADD UNIQUE KEY uk_user_referral_code (referral_code);
```

#### 3.2 應用層檢查

在 `applyReferralForUser()` 增加邏輯：
- 檢查 `user.referral_code IS NOT NULL` → 拋出異常
- 檢查自推薦（推薦碼擁有者 vs userId）

#### 3.3 限速防護

在 `/api/auth/validate-referral` 加 Rate Limit（如用 Spring Cloud Gateway 或 AOP）

```java
@RateLimit(maxRequests = 10, windowSeconds = 60)
@PostMapping("/validate-referral")
public ResponseEntity<...> validateReferral(...) { ... }
```

#### 3.4 審計日誌

記錄所有推薦碼綁定事件到 audit_log

```java
private void logReferralBinding(String userId, String code, String method) {
    AuditLog log = new AuditLog();
    log.setUserId(userId);
    log.setAction("REFERRAL_BIND");
    log.setDetails(String.format("code=%s, method=%s", code, method));
    log.setCreatedAt(LocalDateTime.now());
    auditLogMapper.insert(log);
}
```

---

### 第 4 階段：API 合約 & 測試用例

**預估**：4-6 小時

#### 4.1 撰寫 API 合約

見 `contracts/` 資料夾：
- `POST_api_auth_register.md`（修改）
- `POST_api_auth_validate_referral.md`（新增）
- `POST_api_user_apply_referral.md`（新增）
- `GET_api_user_me.md`（修改）

#### 4.2 撰寫 Controller 測試

`src/test/java/com/group/admin/controller/api/UserControllerReferralTest.java`

測試項：
- 官網註冊 + 有效推薦碼
- 官網註冊 + 無效推薦碼
- OAuth 新用戶補碼
- OAuth 既存用戶不允許改碼

#### 4.3 撰寫 Service 測試

`src/test/java/com/group/admin/service/impl/ReferralCodeServiceImplReferralSignupTest.java`

---

### 第 5 階段：前端文件更新

**預估**：2-3 小時

#### 5.1 更新 API 文件

`frontend/client/01-auth.md`:

```markdown
## 新增欄位：推薦碼

### 官網註冊

POST /api/auth/register
{
  email: "...",
  password: "...",
  confirmPassword: "...",
  nickname: "...",
  referralCode: "ABC123"  // ← 新增（可選）
}

回應：
{
  accessToken: "...",
  isNewUser: false
}
```

#### 5.2 更新 User Profile 文件

`frontend/client/02-user-profile.md`:

新增「Google 新用戶補碼流程」章節

```markdown
#### Google 登入後補碼（新用戶）

GET /api/user/me
回應 { ..., isNewUser: true, referralCode: null }

↓

POST /api/user/apply-referral
{
  code: "STORE-XXXX"
}

回應 204 No Content（成功）
```

---

## 檔案變更清單

### 後端

```
src/main/java/com/group/admin/
├── controller/
│   ├── api/
│   │   ├── ReferralValidationController.java          ← NEW
│   │   ├── UserController.java                        ← MODIFY (add /apply-referral)
│   │   └── ApiAuthController.java                     ← MODIFY
│   │
│   ├── req/
│   │   ├── AuthRegisterReq.java                       ← MODIFY (add referralCode)
│   │   ├── ReferralValidationReq.java                 ← NEW
│   │   └── ApplyReferralReq.java                      ← NEW
│   │
│   ├── res/
│   │   ├── UserRes.java                               ← MODIFY (add isNewUser)
│   │   └── ReferralValidationRes.java                 ← NEW
│   │
│   ├── service/
│   │   ├── UserService.java                           ← MODIFY
│   │   └── impl/
│   │       ├── UserServiceImpl.java                    ← MODIFY
│   │       └── ReferralCodeServiceImpl.java            ← MODIFY
│   │
│   └── entity/
│       └── User.java                                  ← REGENERATE (MBG)

sql/
└── V_2026_04_14__add_referral_signup_fields.sql       ← NEW
```

### 前端文件

```
frontend/client/
├── 01-auth.md                                         ← MODIFY
├── 02-user-profile.md                                 ← MODIFY
└── PROMPT-FOR-FRONTEND.md                             ← MODIFY
```

---

## 測試策略

### 單元測試

- `ReferralCodeServiceImplTest.validateAndGetReferralCode()`
- `UserServiceImplTest.register() with referralCode`
- `UserServiceImplTest.applyReferralForUser()`

### 集成測試

- `UserControllerTest.registerWithValidReferral()`
- `UserControllerTest.registerWithInvalidReferral()`
- `UserControllerTest.applyReferralAsNewOAuthUser()`
- `ReferralValidationControllerTest.validateReferral()`

### 端到端測試（手動）

1. 官網註冊 + 店家推薦碼 ✓ 綁定成功
2. Google 新用戶 → 首頁提示「補碼」→ 補碼成功
3. Google 既存用戶 → 無提示，直接登入
4. 試圖改碼 → 403 Forbidden

---

## 回滾計劃

若遇到重大問題：

1. Git 回滾到上一版本
2. 執行反向 SQL 遷移：

```sql
ALTER TABLE user DROP COLUMN referral_code;
ALTER TABLE user DROP COLUMN referred_store_id;
ALTER TABLE user DROP COLUMN referral_bound_at;
ALTER TABLE user DROP COLUMN is_oauth_new_user;
```

3. 重新生成 MyBatis 檔案
4. 重新部署

---

## 相依性

- ✓ 012-referral-code（推薦碼基礎系統已完成）
- ✓ Spring Security + JWT 認證（已完成）
- ⏳ Google OAuth 整合（應已完成）

