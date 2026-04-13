package com.group.admin.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group.admin.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * OAuth2 認證控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    @GetMapping("/success")
    public ResponseEntity<Map<String, Object>> oauth2LoginSuccess(
            @AuthenticationPrincipal OAuth2User principal) {
        
        if (principal == null) {
            log.warn("OAuth2 login success but principal is null");
            throw new BusinessException("認證失敗");
        }

        String email = principal.getAttribute("email");
        log.info("OAuth2 login success for user: {}", email != null ? email : "unknown");
        
        return ResponseEntity.ok(Map.of(
            "email", principal.getAttribute("email") != null ? principal.getAttribute("email") : "",
            "name", principal.getAttribute("name") != null ? principal.getAttribute("name") : "",
            "message", "OAuth2 登入成功，請實作 JWT token 產生邏輯"
        ));
    }

    @GetMapping("/failure")
    public ResponseEntity<Map<String, String>> oauth2LoginFailure() {
        log.error("OAuth2 login failed");
        throw new BusinessException("OAuth2 登入失敗");
    }
}
