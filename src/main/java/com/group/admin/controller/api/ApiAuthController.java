package com.group.admin.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.group.admin.util.SecurityUtils;

import com.group.admin.entity.User;
import com.group.admin.req.AuthGoogleReq;
import com.group.admin.req.AuthLoginReq;
import com.group.admin.req.AuthRegisterReq;
import com.group.admin.req.RefreshTokenReq;
import com.group.admin.req.auth.ForgotPasswordReq;
import com.group.admin.req.auth.ResetPasswordReq;
import com.group.admin.res.AuthRes;
import com.group.admin.service.UserService;
import com.group.admin.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * - 忘記密碼/重設密碼
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "前台認證", description = "使用者註冊、登入、忘記密碼等功能")
public class ApiAuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 使用者註冊
     * POST /api/auth/register
     */
    @PostMapping("/register")
    @Operation(summary = "使用者註冊", description = "使用 Email 和密碼註冊新帳號")
    public ResponseEntity<AuthRes> register(@Valid @RequestBody AuthRegisterReq req) {
        log.info("用戶註冊請求: {}", req.getEmail());
        
        // 驗證密碼一致性（Controller 層額外檢查）
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            log.warn("密碼確認不一致: {}", req.getEmail());
            throw new IllegalArgumentException("密碼與確認密碼不一致");
        }
        
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
    
    /**
     * 請求重設密碼（發送郵件）
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘記密碼", description = "發送密碼重設郵件到註冊的 Email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordReq req) {
        log.info("📧 忘記密碼請求: email={}", req.getEmail());
        
        try {
            userService.requestPasswordReset(req.getEmail());
            return ResponseEntity.ok(Map.of(
                "message", "如果此 Email 已註冊，將會收到密碼重設郵件"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    /**
     * 重設密碼（使用 token）
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重設密碼", description = "使用郵件中的 token 重設密碼")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        log.info("🔑 重設密碼請求");
        
        // 驗證密碼確認
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "兩次輸入的密碼不一致"
            ));
        }
        
        try {
            userService.resetPassword(req.getToken(), req.getNewPassword());
            return ResponseEntity.ok(Map.of(
                "message", "密碼重設成功，請使用新密碼登入"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "登出", description = "使前台用戶 token 失效")
    public ResponseEntity<Void> logout() {
        String userId = SecurityUtils.getCurrentApiUserId();
        userService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Email 驗證", description = "使用驗證連結完成 Email 驗證")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        boolean ok = userService.verifyEmail(token);
        if (!ok) throw new IllegalArgumentException("驗證連結已失效或不存在");
        return ResponseEntity.ok(Map.of("message", "Email 驗證成功"));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "重新發送驗證郵件", description = "重新發送 Email 驗證郵件")
    public ResponseEntity<Void> resendVerification() {
        String userId = SecurityUtils.getCurrentApiUserId();
        userService.resendVerificationEmail(userId);
        return ResponseEntity.ok().build();
    }
}
