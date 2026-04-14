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
import org.springframework.web.client.RestTemplate;

import com.group.admin.entity.User;
import com.group.admin.example.UserExample;
import com.group.admin.mapper.UserMapper;
import com.group.admin.req.AuthGoogleReq;
import com.group.admin.req.AuthLoginReq;
import com.group.admin.req.AuthRegisterReq;
import com.group.admin.res.AuthRes;
import com.group.admin.service.EmailService;
import com.group.admin.service.ReferralCodeService;
import com.group.admin.service.UserService;
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
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client-id:}")
    private String googleClientId;

    @Override
    public User register(AuthRegisterReq req) {
        // 驗證密碼確認
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("密碼與確認密碼不一致");
        }
        
        // 使用 Example 模式檢查 Email 是否已存在
        User existing = findByEmail(req.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString()); // 使用 UUID
        user.setEmail(req.getEmail());
        user.setNickname(req.getNickname());
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
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("新使用者註冊成功: {}, provider: EMAIL", user.getEmail());
        
        // ✅ 處理推薦碼（如果有提供）
        if (req.getReferralCode() != null && !req.getReferralCode().trim().isEmpty()) {
            log.info("🎁 處理推薦碼: {}", req.getReferralCode());
            // T015: use 3-param version — exceptions propagate as 400 to caller
            referralCodeService.useCode(user.getId(), req.getReferralCode().trim(), req.getEmail());
            log.info("✅ 推薦碼使用成功: userId={}, code={}", user.getId(), req.getReferralCode());
        }
        
        return user;
    }

    @Override
    public AuthRes login(AuthLoginReq req) {
        User user = findByEmail(req.getEmail());
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

        // 檢查是否為 OAuth 用戶（沒有密碼或 provider 不是 EMAIL）
        if (!"EMAIL".equals(user.getProvider())) {
            throw new IllegalArgumentException("Please use " + user.getProvider() + " to login");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // 更新最後登入時間
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateByPrimaryKeySelective(user);

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
                user.setLastLoginAt(LocalDateTime.now());
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(user);
                log.info("Google OAuth 新用戶註冊: {}", email);
            } else {
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

                // 已存在用戶
                if ("EMAIL".equals(user.getProvider())) {
                    // 如果是本地帳號，可以選擇綁定 Google 或拒絕
                    // 這裡選擇允許 Google 登入並更新 provider 資訊
                    user.setProvider("GOOGLE");
                    user.setProviderId(googleId);
                    if (user.getAvatar() == null && picture != null) {
                        user.setAvatar(picture);
                    }
                }
                // 更新登入時間
                user.setLastLoginAt(LocalDateTime.now());
                userMapper.updateByPrimaryKeySelective(user);
                log.info("Google OAuth 用戶登入: {}", email);
            }

            // 生成 Token
            String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), "user", List.of("USER"));
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

            AuthRes res = new AuthRes();
            res.setAccessToken(accessToken);
            res.setRefreshToken(refreshToken);
            res.setExpiresIn(jwtUtil.getExpirationSeconds());
            res.setUser(user);
            return res;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Google OAuth 驗證失敗", ex);
            throw new IllegalArgumentException("Google authentication failed");
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
        User user = findByEmail(email);
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
}
