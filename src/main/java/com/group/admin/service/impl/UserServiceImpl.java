package com.group.admin.service.impl;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.group.admin.entity.User;
import com.group.admin.exception.BusinessException;
import com.group.admin.example.UserExample;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.AuthGoogleReq;
import com.group.admin.req.AuthLoginReq;
import com.group.admin.req.AuthRegisterReq;
import com.group.admin.res.AuthRes;
import com.group.admin.res.referral.ReferralCodeRes;
import com.group.admin.service.EmailService;
import com.group.admin.service.ReferralCodeService;
import com.group.admin.service.UserService;
import com.group.admin.service.LoginHistoryService;
import com.group.admin.service.UserTokenBlacklistService;
import com.group.admin.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 前台使用者服務實作
 * 
 * provider 欄位說明：
 * - EMAIL: 使用 Email + 密碼註冊的本地帳號
 * - GOOGLE: 使用 Google OAuth2 登入的帳號
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ReferralCodeService referralCodeService;
    private final EmailService emailService;
    private final LoginHistoryService loginHistoryService;
    private final UserTokenBlacklistService userTokenBlacklistService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client-id:}")
    private String googleClientId;

    @Override
    @Transactional
    public User register(AuthRegisterReq req) {
        // 驗證密碼確認
        if (!req.getPassword().equals(req.getConfirmedPassword())) {
            throw new IllegalArgumentException("密碼與確認密碼不一致");
        }
        
        // 使用 Example 模式檢查 Email 是否已存在
        User existing = findByEmail(req.getEmail());
        if (existing != null) {
            throw new BusinessException("CONFLICT", "此 Email 已被註冊");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString()); // 使用 UUID
        user.setEmail(req.getEmail());
        user.setNickname(resolveNickname(req));
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setProvider("EMAIL"); // 本地註冊
        user.setGoldCoins(0L);
        user.setBonusCoins(0L);
        user.setTotalRecharged(0L);
        user.setVersion(0);
        user.setStatus("ACTIVE");
        user.setEmailVerified((byte) 0);
        
        // ✅ 手機號碼與 LINE ID
        user.setPhoneNumber(req.getPhoneNumber());
        user.setLineId(req.getLineId());
        
        // ✅ 頭像
        user.setAvatar(req.getAvatar());
        
        // ✅ 收件地址資訊
        user.setRecipientName(req.getAddressName());
        user.setCity(req.getCity());
        user.setDistrict(req.getArea()); // area → district
        user.setAddressDetail(req.getAddress());
        // zipCode 目前 User entity 沒有此欄位，如需要請先更新資料表
        
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("✅ 新使用者註冊成功: {}, provider: EMAIL", user.getEmail());

        // ✅ 發送 Email 驗證信（讓用戶登出後仍能再登入）
        try {
            String verificationToken = UUID.randomUUID().toString();
            User verifyUpdate = new User();
            verifyUpdate.setId(user.getId());
            verifyUpdate.setEmailVerificationToken(verificationToken);
            verifyUpdate.setEmailVerificationExpires(LocalDateTime.now().plusHours(24));
            userMapper.updateByPrimaryKeySelective(verifyUpdate);
            // 同步到 user 物件供後續使用
            user.setEmailVerificationToken(verificationToken);
            emailService.sendVerificationEmail(user.getEmail(), user.getNickname(), verificationToken);
            log.info("📧 驗證信已發送: email={}", user.getEmail());
        } catch (Exception e) {
            // 驗證信發送失敗不影響註冊流程（用戶可以從前端觸發重送）
            log.warn("⚠️ 驗證信發送失敗（不影響註冊）: email={}, error={}", user.getEmail(), e.getMessage());
        }

        // ✅ 處理推薦碼（如果有提供）
        if (req.getReferralCode() != null && !req.getReferralCode().trim().isEmpty()) {
            log.info("🎁 處理推薦碼: {}", req.getReferralCode());
            try {
                String code = req.getReferralCode().trim().toUpperCase();
                boolean used = referralCodeService.useCode(user.getId(), code);
                if (used) {
                    // 同步推薦碼資訊到 user 表（新增）
                    ReferralCodeRes codeRes = referralCodeService.getByCode(code);
                    if (codeRes != null) {
                        user.setReferralCode(code);
                        user.setReferredStoreId(codeRes.getStoreId());
                        user.setReferralBoundAt(LocalDateTime.now());
                        userMapper.updateByPrimaryKeySelective(user);
                        log.info("✅ 推薦碼綁定成功: userId={}, code={}, storeId={}",
                                user.getId(), code, codeRes.getStoreId());
                    }
                } else {
                    log.warn("⚠️ 推薦碼無效或已停用: {}", req.getReferralCode());
                }
            } catch (Exception e) {
                // 推薦碼處理失敗不影響註冊
                log.warn("⚠️ 推薦碼處理失敗: {}", e.getMessage());
            }
        }
        
        return user;
    }

    @Override
    public AuthRes login(AuthLoginReq req) {
        User user = normalizeLegacyProvider(findByEmail(req.getEmail()));
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // ✅ 檢查會員狀態（停用或刪除的會員不能登入）
        if ("INACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("帳號已被停用，請聯繫客服");
        }
        if ("DELETED".equals(user.getStatus())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if ("SUSPENDED".equals(user.getStatus())) {
            throw new IllegalArgumentException("帳號已被暫停使用，請聯繫客服");
        }

        // 帳號鎖定檢查
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            loginHistoryService.record(user.getId(), "user", "EMAIL", "LOCKED", "帳號已鎖定", null, null);
            throw new IllegalArgumentException("帳號已鎖定，請於 " + user.getLockedUntil().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + " 後再試");
        }

        // 檢查是否為 OAuth 用戶（沒有密碼或 provider 不是 EMAIL）
        if (!"EMAIL".equals(user.getProvider())) {
            throw new IllegalArgumentException("此帳號使用 " + user.getProvider() + " 登入，請使用對應登入方式");
        }

        // Email 驗證檢查（EMAIL provider 才需要）
        if (user.getEmailVerified() == null || user.getEmailVerified() == 0) {
            throw new IllegalArgumentException("請先完成 Email 驗證");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            User lockUpdate = new User();
            lockUpdate.setId(user.getId());
            lockUpdate.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                lockUpdate.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                log.warn("🔒 用戶帳號已鎖定 15 分鐘: email={}", req.getEmail());
            }
            userMapper.updateByPrimaryKeySelective(lockUpdate);
            loginHistoryService.record(user.getId(), "user", "EMAIL", "FAILED", "密碼錯誤", null, null);
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 重設失敗次數並更新最後登入時間
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateByPrimaryKey(user);
        loginHistoryService.record(user.getId(), "user", "EMAIL", "SUCCESS", null, null, null);

        // 生成 Token（包含 userId 和 userType）
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), "user", List.of("USER"));
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        AuthRes res = new AuthRes();
        res.setAccessToken(accessToken);
        res.setRefreshToken(refreshToken);
        res.setExpiresIn(jwtUtil.getExpirationSeconds());
        res.setUser(user);
        return res;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public AuthRes loginWithGoogle(AuthGoogleReq req) {
        if (req == null || req.getIdToken() == null || req.getIdToken().isBlank()) {
            throw new IllegalArgumentException("Invalid Google token");
        }

        try {
            // 驗證 Google ID Token
            URI uri = new URI("https://oauth2.googleapis.com/tokeninfo?id_token=" + req.getIdToken());
            ResponseEntity<Map> resp = restTemplate.getForEntity(uri, Map.class);
            Map<String, Object> body = resp.getBody();
            
            if (body == null || body.get("email") == null) {
                throw new IllegalArgumentException("Invalid Google token");
            }
            
            String email = (String) body.get("email");
            String googleId = (String) body.get("sub"); // Google 唯一用戶 ID
            String picture = (String) body.getOrDefault("picture", null);
            String name = (String) body.getOrDefault("name", email.split("@")[0]);
            
            // 查詢是否已有此 Email 的用戶
            User user = findByEmail(email);
            
            if (user == null) {
                // 新用戶：使用 Google 註冊
                user = new User();
                user.setId(UUID.randomUUID().toString());
                user.setEmail(email);
                user.setNickname(name);
                user.setAvatar(picture);
                user.setProvider("GOOGLE"); // Google 登入
                user.setProviderId(googleId); // 存儲 Google 用戶 ID
                user.setGoldCoins(0L);
                user.setBonusCoins(0L);
                user.setTotalRecharged(0L);
                user.setVersion(0);
                user.setStatus("ACTIVE");
                user.setEmailVerified((byte) 1); // Google 已驗證 Email
                user.setFailedLoginAttempts(0);
                user.setLastLoginAt(LocalDateTime.now());
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                user.setIsOauthNewUser(1); // ⭐ 標記為 OAuth 新用戶，前端用於顯示補推薦碼引導
                userMapper.insert(user);
                log.info("Google OAuth 新用戶註冊: {}, userId={}", email, user.getId());
            } else {
                user = normalizeLegacyProvider(user);
                // ✅ 帳號衝突檢查：EMAIL provider 帳號不能用 Google 登入（雙向不混用原則）
                if (!"GOOGLE".equals(user.getProvider())) {
                    log.warn("⚠️ 帳號衝突：email={} 已使用 {} 方式註冊，試圖用 Google 登入", email, user.getProvider());
                    throw new BusinessException(
                        "EMAIL_PROVIDER_CONFLICT",
                        "此 Email 已用 Email/密碼方式註冊，請改用密碼登入"
                    );
                }

                // ✅ 檢查會員狀態（停用或刪除的會員不能登入）
                if ("INACTIVE".equals(user.getStatus())) {
                    throw new IllegalArgumentException("帳號已被停用，請聯繫客服");
                }
                if ("DELETED".equals(user.getStatus())) {
                    throw new IllegalArgumentException("帳號不存在");
                }
                if ("SUSPENDED".equals(user.getStatus())) {
                    throw new IllegalArgumentException("帳號已被暫停使用，請聯繫客服");
                }

                // 更新登入時間
                user.setLastLoginAt(LocalDateTime.now());
                userMapper.updateByPrimaryKeySelective(user);
                log.info("✅ Google OAuth 用戶登入: {}", email);
            }

            // 生成 Token
            String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), "user", List.of("USER"));
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            boolean isNewUser = Integer.valueOf(1).equals(user.getIsOauthNewUser());

            AuthRes res = new AuthRes();
            res.setAccessToken(accessToken);
            res.setRefreshToken(refreshToken);
            res.setExpiresIn(jwtUtil.getExpirationSeconds());
            res.setUser(user);
            res.setIsNewUser(isNewUser); // ⭐ 前端用於判斷是否要顯示補推薦碼引導
            return res;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Google OAuth 驗證失敗", ex);
            throw new IllegalArgumentException("Google authentication failed");
        }
    }

    /**
     * OAuth 新用戶補推薦碼（一次性，已綁定則拋例外）
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void applyReferral(String userId, String code) {
        log.info("🎁 [applyReferral] userId={}, code={}", userId, code);

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new IllegalArgumentException("用戶不存在");
        }
        // 已綁定推薦碼，不允許重複
        if (user.getReferralCode() != null && !user.getReferralCode().isEmpty()) {
            throw new IllegalArgumentException("您已綁定過推薦碼，無法再次修改");
        }
        String upperCode = code.trim().toUpperCase();
        boolean used = referralCodeService.useCode(userId, upperCode);
        if (!used) {
            throw new IllegalArgumentException("推薦碼無效、已停用或已超過使用上限");
        }
        // 同步推薦碼資訊到 user 表
        ReferralCodeRes codeRes = referralCodeService.getByCode(upperCode);
        if (codeRes != null) {
            user.setReferralCode(upperCode);
            user.setReferredStoreId(codeRes.getStoreId());
            user.setReferralBoundAt(LocalDateTime.now());
            user.setIsOauthNewUser(0); // 已完成補碼，清除新用戶旗標
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateByPrimaryKeySelective(user);
            log.info("✅ OAuth 補推薦碼成功: userId={}, code={}, storeId={}", userId, upperCode, codeRes.getStoreId());
        }
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        UserExample example = new UserExample();
        example.createCriteria().andEmailEqualTo(email);
        List<User> users = userMapper.selectByExample(example);
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public User findById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public void requestPasswordReset(String email) {
        log.info("📧 請求重設密碼: email={}", email);
        
        // 查詢使用者
        User user = normalizeLegacyProvider(findByEmail(email));
        if (user == null) {
            // 為了安全，即使使用者不存在也不報錯
            log.warn("⚠️ 使用者不存在，但不回報錯誤: email={}", email);
            return;
        }
        
        // 只有 EMAIL provider 的帳號才能重設密碼
        if (!"EMAIL".equals(user.getProvider())) {
            log.warn("⚠️ 非 EMAIL 帳號無法重設密碼: email={}, provider={}", email, user.getProvider());
            throw new IllegalArgumentException("此帳號使用第三方登入，無法重設密碼");
        }
        
        // 生成重設 token（UUID）
        String resetToken = UUID.randomUUID().toString();
        
        // 更新使用者的 password_reset_token 和過期時間（1小時後）
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKey(user);
        
        // 發送密碼重設郵件
        emailService.sendPasswordResetEmail(email, user.getNickname(), resetToken);
        
        log.info("✅ 密碼重設郵件已發送: email={}, token={}", email, resetToken);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("🔑 執行密碼重設: token={}", token);
        
        // 查詢有此 token 且未過期的使用者
        UserExample example = new UserExample();
        example.createCriteria()
               .andPasswordResetTokenEqualTo(token)
               .andPasswordResetExpiresGreaterThan(LocalDateTime.now());
        
        List<User> users = userMapper.selectByExample(example);
        
        if (users.isEmpty()) {
            log.warn("❌ 無效或已過期的 token: {}", token);
            throw new IllegalArgumentException("重設連結無效或已過期");
        }
        
        User user = users.get(0);
        
        // 更新密碼並清除 token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKey(user);
        
        log.info("✅ 密碼重設成功: userId={}, email={}", user.getId(), user.getEmail());
    }

    @Override
    public void updateUser(User user) {
        userMapper.updateByPrimaryKey(user);
    }

    @Override
    public void logout(String userId) {
        userTokenBlacklistService.invalidateUserTokens(userId);
        log.info("✅ 前台用戶已登出: userId={}", userId);
    }

    @Override
    public boolean verifyEmail(String token) {
        UserExample example = new UserExample();
        example.createCriteria()
            .andEmailVerificationTokenEqualTo(token)
            .andEmailVerificationExpiresGreaterThan(LocalDateTime.now());
        List<User> users = userMapper.selectByExample(example);
        if (users.isEmpty()) return false;
        User user = users.get(0);
        User update = new User();
        update.setId(user.getId());
        update.setEmailVerified((byte) 1);
        update.setEmailVerificationToken(null);
        update.setEmailVerificationExpires(null);
        userMapper.updateByPrimaryKeySelective(update);
        log.info("✅ Email 驗證成功: userId={}", user.getId());
        return true;
    }

    @Override
    public void resendVerificationEmail(String userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) throw new IllegalArgumentException("用戶不存在");
        if (user.getEmailVerified() != null && user.getEmailVerified() == 1) {
            throw new IllegalArgumentException("Email 已完成驗證");
        }
        String verificationToken = UUID.randomUUID().toString();
        User update = new User();
        update.setId(userId);
        update.setEmailVerificationToken(verificationToken);
        update.setEmailVerificationExpires(LocalDateTime.now().plusHours(24));
        userMapper.updateByPrimaryKeySelective(update);
        emailService.sendVerificationEmail(user.getEmail(), user.getNickname(), verificationToken);
        log.info("✅ 重新發送驗證郵件: userId={}", userId);
    }

    private String resolveNickname(AuthRegisterReq req) {
        if (req.getNickname() != null && !req.getNickname().isBlank()) {
            return req.getNickname().trim();
        }
        return req.getEmail().split("@")[0];
    }

    private User normalizeLegacyProvider(User user) {
        if (user == null) {
            return null;
        }

        boolean hasPassword = user.getPassword() != null && !user.getPassword().isBlank();
        boolean looksLikeCorruptedGoogle = "GOOGLE".equals(user.getProvider()) && hasPassword;
        boolean looksLikeLegacyEmail = (user.getProvider() == null || user.getProvider().isBlank()) && hasPassword;

        if (!looksLikeCorruptedGoogle && !looksLikeLegacyEmail) {
            return user;
        }

        User update = new User();
        update.setId(user.getId());
        update.setProvider("EMAIL");
        update.setProviderId(null);
        if (user.getEmailVerified() == null || user.getEmailVerified() == 0) {
            update.setEmailVerified((byte) 1);
            user.setEmailVerified((byte) 1);
        }
        update.setUpdatedAt(LocalDateTime.now());
        userMapper.updateByPrimaryKeySelective(update);

        user.setProvider("EMAIL");
        user.setProviderId(null);
        log.warn("🔧 修復舊會員 provider 異常: email={}, repairedTo=EMAIL", user.getEmail());
        return user;
    }
}
