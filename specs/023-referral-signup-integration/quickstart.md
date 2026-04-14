# 快速入門：推薦碼於使用者註冊流程整合

**功能分支**：`023-referral-signup-integration`  
**預計時間**：3-4 工作日  

---

## 快速概覽

本功能實現推薦碼在使用者註冊流程中的完整集成，分兩條路徑：

```
Route A: 官網 Email 註冊
  ├─ 前端：註冊表單新增推薦碼欄位（可選）
  ├─ 後端：POST /api/auth/register { email, password, referralCode }
  └─ 結果：一次性綁定，註冊完成

Route B: Google OAuth 
  ├─ 新用戶：可後續補碼
  │  ├─ 登入後收到 isNewUser=true
  │  ├─ 進入新用戶導覽彈窗
  │  └─ POST /api/user/apply-referral { code } 補綁
  └─ 既存用戶：直接登入，不允許改碼
```

---

## 開發清單（分工）

### 後端工程師

以下範例假設已完成 MySQL 遷移和 MBG 重新生成。

#### Step 1：創建驗證 DTO

#### 檔案：`src/main/java/com/group/admin/req/ReferralValidationReq.java`

```java
package com.group.admin.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReferralValidationReq {
    @NotBlank(message = "推薦碼不能為空")
    @Size(max = 50, message = "推薦碼長度不超過 50 字元")
    private String code;
}
```

#### 檔案：`src/main/java/com/group/admin/res/ReferralValidationRes.java`

```java
package com.group.admin.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferralValidationRes {
    private boolean valid;
    private String code;
    private String storeName;  // 若有效，回傳店家名稱
    private String reason;     // 若無效，回傳原因
}
```

---

#### Step 2：修改 AuthRegisterReq

檔案：`src/main/java/com/group/admin/req/AuthRegisterReq.java`

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
    private String referralCode;  // 可選，留空視同無推薦
}
```

---

#### Step 3：建立驗證 Controller

檔案：`src/main/java/com/group/admin/controller/api/ReferralValidationController.java`

```java
package com.group.admin.controller.api;

import com.group.admin.entity.ReferralCode;
import com.group.admin.entity.Store;
import com.group.admin.mapper.ReferralCodeMapper;
import com.group.admin.mapper.StoreMapper;
import com.group.admin.req.ReferralValidationReq;
import com.group.admin.res.ReferralValidationRes;
import com.group.admin.service.ReferralCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "前台認證", description = "推薦碼驗證相關")
public class ReferralValidationController {
    
    private final ReferralCodeService referralCodeService;
    private final ReferralCodeMapper referralCodeMapper;
    private final StoreMapper storeMapper;
    
    /**
     * POST /api/auth/validate-referral
     * 公開端點：驗證推薦碼有效性（用於註冊表單）
     */
    @PostMapping("/validate-referral")
    @Operation(summary = "驗證推薦碼", description = "在註冊表單中驗證推薦碼有效性")
    public ResponseEntity<ReferralValidationRes> validateReferral(
            @Valid @RequestBody ReferralValidationReq req) {
        
        String code = req.getCode().trim().toUpperCase();
        log.info("🔍 [ReferralValidation] 驗證推薦碼: {}", code);
        
        // 呼叫驗證邏輯
        ReferralCode referralCode = referralCodeService.validateAndGetReferralCode(code);
        
        if (referralCode == null) {
            log.warn("❌ 推薦碼無效或已停用: {}", code);
            return ResponseEntity.ok(ReferralValidationRes.builder()
                    .valid(false)
                    .code(code)
                    .reason("推薦碼不存在或已停用")
                    .build());
        }
        
        // 取得店家名稱
        Store store = storeMapper.selectByPrimaryKey(referralCode.getStoreId());
        String storeName = store != null ? store.getStoreName() : "未知店家";
        
        log.info("✅ 推薦碼有效: code={}, store={}", code, storeName);
        return ResponseEntity.ok(ReferralValidationRes.builder()
                .valid(true)
                .code(code)
                .storeName(storeName)
                .build());
    }
}
```

---

#### Step 4：擴充 ReferralCodeService

檔案：`src/main/java/com/group/admin/service/impl/ReferralCodeServiceImpl.java`

新增以下方法：

```java
/**
 * 驗證推薦碼有效性（用於註冊）
 * 不拋異常，回傳 null 表無效
 */
public ReferralCode validateAndGetReferralCode(String code) {
    log.info("🔍 驗證推薦碼: {}", code);
    
    // 1. 查詢推薦碼
    ReferralCode rc = referralCodeRepository.selectByCode(code.toUpperCase());
    if (rc == null) {
        log.warn("❌ 推薦碼不存在");
        return null;
    }
    
    // 2. 檢查活躍狀態
    if (!Boolean.TRUE.equals(rc.getIsActive())) {
        log.warn("❌ 推薦碼已停用");
        return null;
    }
    
    // 3. 檢查使用次數限制
    if (rc.getMaxUsage() != null && rc.getUsedCount() >= rc.getMaxUsage()) {
        log.warn("❌ 推薦碼已達使用上限");
        return null;
    }
    
    // 4. 檢查有效期
    if (rc.getExpiresAt() != null && rc.getExpiresAt().isBefore(LocalDateTime.now())) {
        log.warn("❌ 推薦碼已過期");
        return null;
    }
    
    // 5. 檢查店家是否活躍
    Store store = storeMapper.selectByPrimaryKey(rc.getStoreId());
    if (store == null || !"ACTIVE".equals(store.getStatus())) {
        log.warn("❌ 店家不活躍");
        return null;
    }
    
    log.info("✅ 推薦碼驗證通過");
    return rc;
}

/**
 * 為 OAuth 新用戶補上推薦碼（一次性）
 */
@Transactional
public void applyReferralForUser(String userId, String code) {
    log.info("🎁 為用戶補推薦碼: userId={}, code={}", userId, code);
    
    // 1. 查詢用戶
    User user = userMapper.selectByPrimaryKey(userId);
    if (user == null) {
        throw new BusinessException("用戶不存在");
    }
    
    // 2. 檢查是否已綁定推薦碼（一次性防護）
    if (user.getReferralCode() != null && !user.getReferralCode().isEmpty()) {
        log.warn("❌ 推薦碼已綁定，無法修改: userId={}, existing={}", userId, user.getReferralCode());
        throw new BusinessException("推薦碼已綁定，無法修改");
    }
    
    // 3. 驗證推薦碼
    ReferralCode rc = validateAndGetReferralCode(code.toUpperCase());
    if (rc == null) {
        throw new BusinessException("推薦碼無效或已停用");
    }
    
    // 4. 防自推薦（可根據業務邏輯進一步細化）
    if (rc.getOwnerId().equals(userId)) {
        throw new BusinessException("不能使用自己的推薦碼");
    }
    
    // 5. 更新用戶記錄
    user.setReferralCode(code.toUpperCase());
    user.setReferredStoreId(rc.getStoreId());
    user.setReferralBoundAt(LocalDateTime.now());
    userMapper.updateByPrimaryKey(user);
    
    // 6. 建立推薦紀錄
    createReferralRecord(user, code.toUpperCase(), "OAUTH");
    
    // 7. 遞增推薦碼使用計數
    referralCodeMapper.incrementUsageCount(rc.getId());
    
    log.info("✅ 推薦碼綁定成功: userId={}, code={}", userId, code);
}

/**
 * 建立推薦紀錄
 */
private void createReferralRecord(User user, String code, String signupMethod) {
    ReferralRecord record = new ReferralRecord();
    record.setId(UUID.randomUUID().toString());
    record.setUserId(user.getId());
    record.setReferralCode(code);
    record.setStoreId(user.getReferredStoreId());
    record.setSignupMethod(signupMethod);  // EMAIL 或 OAUTH
    record.setCreatedAt(LocalDateTime.now());
    referralRecordMapper.insert(record);
    log.info("📝 推薦紀錄已建立: recordId={}", record.getId());
}
```

---

#### Step 5：修改 UserService.register()

檔案：`src/main/java/com/group/admin/service/impl/UserServiceImpl.java`

```java
@Transactional
public User register(AuthRegisterReq req) {
    log.info("👤 使用者註冊: email={}", req.getEmail());
    
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
        ReferralCode validCode = referralCodeService.validateAndGetReferralCode(
            req.getReferralCode().toUpperCase());
        
        if (validCode == null) {
            throw new BusinessException("推薦碼無效或已停用");
        }
        
        referralCode = req.getReferralCode().toUpperCase();
        referredStoreId = validCode.getStoreId();
        log.info("✅ 推薦碼驗證通過: code={}, store={}", referralCode, referredStoreId);
    }
    
    // 4. 建立用戶
    User user = new User();
    user.setId(UUID.randomUUID().toString());
    user.setEmail(req.getEmail());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setNickname(req.getNickname());
    user.setProvider("EMAIL");
    user.setIsActive((byte) 1);
    
    // 設定推薦碼相關欄位
    user.setReferralCode(referralCode);
    user.setReferredStoreId(referredStoreId);
    if (referralCode != null) {
        user.setReferralBoundAt(LocalDateTime.now());
    }
    user.setIsOauthNewUser(0);  // EMAIL 註冊不是 OAuth
    
    userMapper.insert(user);
    log.info("✅ 使用者建立成功: userId={}", user.getId());
    
    // 5. 建立推薦紀錄（若有推薦碼）
    if (referralCode != null) {
        referralCodeService.createReferralRecord(user, referralCode, "EMAIL");
        referralCodeMapper.incrementUsageCount(...);  // 增加使用計數
    }
    
    return user;
}
```

---

#### Step 6：修改 Google OAuth 登入

檔案：`src/main/java/com/group/admin/service/impl/UserServiceImpl.java`

```java
@Transactional
public AuthRes loginWithGoogle(AuthGoogleReq req) {
    log.info("🔑 Google OAuth 登入請求");
    
    // ... 驗證 Google ID Token
    GoogleIdToken.Payload payload = ... ;  // 假設已驗證
    
    String email = payload.getEmail();
    User existingUser = userMapper.selectByEmail(email);
    
    if (existingUser == null) {
        // 新用戶：建立帳號
        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setEmail(email);
        newUser.setProvider("GOOGLE");
        newUser.setNickname((String) payload.get("name"));
        newUser.setAvatar((String) payload.get("picture"));
        newUser.setIsActive((byte) 1);
        newUser.setIsOauthNewUser(1);  // ← 標記新用戶
        newUser.setCreatedAt(LocalDateTime.now());
        
        userMapper.insert(newUser);
        log.info("✅ OAuth 新用戶已建立: userId={}", newUser.getId());
        
        // 回傳含 isNewUser 標記
        String accessToken = jwtUtil.generateToken(email, newUser.getId(), "user", List.of("ROLE_USER"));
        return AuthRes.builder()
                .accessToken(accessToken)
                .refreshToken(jwtUtil.generateRefreshToken(email))
                .expiresIn(jwtUtil.getExpirationSeconds())
                .isNewUser(true)  // ← 前端據此判斷進導覽
                .build();
    } else {
        // 既存用戶：直接登入
        log.info("ℹ️ OAuth 既存用戶登入: userId={}", existingUser.getId());
        
        String accessToken = jwtUtil.generateToken(email, existingUser.getId(), "user", List.of("ROLE_USER"));
        return AuthRes.builder()
                .accessToken(accessToken)
                .refreshToken(jwtUtil.generateRefreshToken(email))
                .expiresIn(jwtUtil.getExpirationSeconds())
                .isNewUser(false)  // ← 不進導覽
                .build();
    }
}
```

---

#### Step 7：新增補碼端點

檔案：`src/main/java/com/group/admin/controller/api/UserController.java`

```java
@PostMapping("/apply-referral")
@Operation(summary = "補上推薦碼", description = "OAuth 新用戶登入後補上推薦碼（一次性）")
public ResponseEntity<Void> applyReferral(
        @Valid @RequestBody ApplyReferralReq req) {
    
    String userId = SecurityUtils.getCurrentUserId();
    if (userId == null) {
        return ResponseEntity.status(401).build();
    }
    
    log.info("🎁 用戶補推薦碼: userId={}, code={}", userId, req.getCode());
    referralCodeService.applyReferralForUser(userId, req.getCode());
    
    return ResponseEntity.ok().build();
}
```

---

### 前端工程師

#### Route A：官網註冊表單

```typescript
interface RegisterForm {
  email: string;
  password: string;
  confirmPassword: string;
  nickname: string;
  referralCode?: string;  // 新增（可選）
}

// 驗證推薦碼（實時檢查）
async function validateReferral(code: string) {
  const response = await axios.post('/api/auth/validate-referral', { code });
  // response.data.data.valid, storeName
}

// 提交註冊
async function handleRegister(formData: RegisterForm) {
  try {
    const response = await axios.post('/api/auth/register', formData);
    // response.data.data = { accessToken, refreshToken, user }
    localStorage.setItem('accessToken', response.data.data.accessToken);
    router.push('/home');
  } catch (error) {
    // 推薦碼無效 → 顯示錯誤
  }
}
```

#### Route B：Google OAuth 新用戶導覽

```typescript
// 登入後判斷是否新用戶
async function handleGoogleLogin(idToken: string) {
  const response = await axios.post('/api/auth/google', { idToken });
  const { accessToken, isNewUser } = response.data.data;
  
  localStorage.setItem('accessToken', accessToken);
  
  if (isNewUser) {
    // 進新用戶導覽彈窗
    showNewUserGuideModal();
  } else {
    router.push('/home');
  }
}

// 新用戶導覽：補碼（可選）
async function applyReferralCode(code: string) {
  try {
    await axios.post('/api/user/apply-referral', { code });
    showToast('推薦碼已綁定！');
    router.push('/home');
  } catch (error) {
    showToast(error.response.data.error);  // 推薦碼無效或已綁定
  }
}
```

---

## 測試驗收清單

- [ ] 官網註冊 + 有效推薦碼 → 成功
- [ ] 官網註冊 + 無效推薦碼 → 失敗，顯示原因
- [ ] 官網註冊無推薦碼 → 成功
- [ ] Google 新用戶 → 收到 isNewUser=true
- [ ] Google 新用戶補碼 → 成功
- [ ] Google 既存用戶 → 無新用戶導覽
- [ ] 試圖改推薦碼 → 403 Forbidden
- [ ] 推薦碼驗證端點限速有效

---

## 相關文件

- Spec：`specs/023-referral-signup-integration/spec.md`
- Plan：`specs/023-referral-signup-integration/plan.md`
- Tasks：`specs/023-referral-signup-integration/tasks.md`
- Contracts：`specs/023-referral-signup-integration/contracts/`

