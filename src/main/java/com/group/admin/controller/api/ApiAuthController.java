package com.group.admin.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group.admin.entity.User;
import com.group.admin.req.AuthGoogleReq;
import com.group.admin.req.AuthLoginReq;
import com.group.admin.req.AuthRegisterReq;
import com.group.admin.req.RefreshTokenReq;
import com.group.admin.res.AuthRes;
import com.group.admin.service.UserService;
import com.group.admin.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 前台認證 API
 * 
 * URL: /api/auth/**
 * 角色：公開（無需登入）
 * 
 * 支援：
 * - Email + 密碼註冊/登入 (provider = EMAIL)
 * - Google OAuth2 登入 (provider = GOOGLE)
 * - Token 刷新
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 使用者註冊
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthRes> register(@Valid @RequestBody AuthRegisterReq req) {
        log.info("用戶註冊請求: {}", req.getEmail());
        User user = userService.register(req);
        
        // 註冊成功後直接返回 Token
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), "user", List.of("USER"));
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        AuthRes res = new AuthRes();
        res.setAccessToken(accessToken);
        res.setRefreshToken(refreshToken);
        res.setExpiresIn(jwtUtil.getExpirationSeconds());
        res.setUser(user);
        
        return ResponseEntity.ok(res);
    }

    /**
     * 使用者登入
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthRes> login(@Valid @RequestBody AuthLoginReq req) {
        log.info("用戶登入請求: {}", req.getEmail());
        AuthRes res = userService.login(req);
        return ResponseEntity.ok(res);
    }

    /**
     * Google OAuth2 登入
     * POST /api/auth/google
     * 
     * 前端取得 Google ID Token 後傳入
     */
    @PostMapping("/google")
    public ResponseEntity<AuthRes> google(@Valid @RequestBody AuthGoogleReq req) {
        log.info("Google OAuth 登入請求");
        AuthRes res = userService.loginWithGoogle(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 刷新 Access Token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthRes> refresh(@Valid @RequestBody RefreshTokenReq req) {
        log.info("Token 刷新請求");
        
        // 驗證 Refresh Token
        if (!jwtUtil.validateToken(req.getRefreshToken())) {
            log.warn("無效的 Refresh Token");
            return ResponseEntity.status(401).body(null);
        }
        
        String email = jwtUtil.getUsername(req.getRefreshToken());
        User user = userService.findByEmail(email);
        
        if (user == null) {
            log.warn("用戶不存在: {}", email);
            return ResponseEntity.status(401).body(null);
        }

        // 生成新的 Access Token
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getId(), "user", List.of("USER"));
        
        // Refresh Token 保持不變（或可以選擇一併刷新）
        String refreshToken = req.getRefreshToken();

        AuthRes res = new AuthRes();
        res.setAccessToken(accessToken);
        res.setRefreshToken(refreshToken);
        res.setExpiresIn(jwtUtil.getExpirationSeconds());
        res.setUser(user);
        
        return ResponseEntity.ok(res);
    }
}
