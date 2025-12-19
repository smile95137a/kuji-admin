package com.group.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group.admin.result.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * OAuth2 認證控制器
 * 處理 OAuth2 登入成功/失敗的回調
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    /**
     * OAuth2 登入成功後的處理
     * 
     * @param principal OAuth2 使用者資訊
     * @return 使用者資訊和 JWT token
     */
    @GetMapping("/success")
    public ResponseEntity<ApiResponse<Map<String, Object>>> oauth2LoginSuccess(
            @AuthenticationPrincipal OAuth2User principal) {
        
        if (principal == null) {
            log.warn("OAuth2 login success but principal is null");
            return ResponseEntity.ok(ApiResponse.error("401", "認證失敗"));
        }

        String email = principal.getAttribute("email");
        log.info("OAuth2 login success for user: {}", email != null ? email : "unknown");
        
        // TODO: 實作以下邏輯
        // 1. 從 principal 取得 email, name 等資訊
        // 2. 檢查使用者是否已存在，不存在則建立新使用者
        // 3. 產生 JWT token
        // 4. 回傳 token 和使用者資訊
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "email", principal.getAttribute("email"),
            "name", principal.getAttribute("name"),
            "message", "OAuth2 登入成功，請實作 JWT token 產生邏輯"
        )));
    }

    /**
     * OAuth2 登入失敗的處理
     */
    @GetMapping("/failure")
    public ResponseEntity<ApiResponse<String>> oauth2LoginFailure() {
        log.error("OAuth2 login failed");
        return ResponseEntity.ok(ApiResponse.error("401", "OAuth2 登入失敗"));
    }
}
