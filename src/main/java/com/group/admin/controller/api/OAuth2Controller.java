package com.group.admin.controller.api;

import com.group.admin.req.AuthGoogleReq;
import com.group.admin.res.AuthRes;
import com.group.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台 OAuth2 認證控制器
 *
 * <p>採前端主導（A 方案）：前端取得 Google ID Token 後 POST 給後端，
 * 後端向 Google 驗證並簽發系統 JWT。</p>
 *
 * <p>後台（/admin/**）不支援 OAuth，僅前台使用。</p>
 */
@Slf4j
@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final UserService userService;

    /**
     * Google OAuth2 登入 / 自動註冊
     *
     * <p>前端取得 Google ID Token 後呼叫此 API。
     * 後端向 Google tokeninfo endpoint 驗證 token，
     * 查詢或建立帳號，並簽發系統 JWT。</p>
     *
     * <p>帳號衝突規則：同一 Email 只能有一種 provider，
     * 若 Email 已用 local 方式註冊則回傳 409 EMAIL_PROVIDER_CONFLICT。</p>
     *
     * @param req 包含 Google ID Token
     * @return AuthRes（accessToken, refreshToken, isNewUser, user）
     */
    @PostMapping("/google")
    public ResponseEntity<AuthRes> googleLogin(@RequestBody AuthGoogleReq req) {
        log.info("🔍 Google OAuth2 登入請求");
        AuthRes res = userService.loginWithGoogle(req);
        log.info("✅ Google OAuth2 登入成功, isNewUser={}", res.getIsNewUser());
        return ResponseEntity.ok(res);
    }
}
