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
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${google.client-id:}")
    private String googleClientId;

    @Override
    public User register(AuthRegisterReq req) {
        // 使用 Example 模式檢查 Email 是否已存在
        User existing = findByEmail(req.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString()); // 使用 UUID
        user.setEmail(req.getEmail());
        user.setNickname(req.getNickname() == null ? req.getEmail().split("@")[0] : req.getNickname());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setProvider("EMAIL"); // 本地註冊
        user.setGoldCoins(0L);
        user.setBonusCoins(0L);
        user.setStatus("ACTIVE");
        user.setEmailVerified(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("新使用者註冊成功: {}, provider: EMAIL", user.getEmail());
        return user;
    }

    @Override
    public AuthRes login(AuthLoginReq req) {
        User user = findByEmail(req.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
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
                user.setStatus("ACTIVE");
                user.setEmailVerified(1); // Google 已驗證 Email
                user.setLastLoginAt(LocalDateTime.now());
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(user);
                log.info("Google OAuth 新用戶註冊: {}", email);
            } else {
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
}
