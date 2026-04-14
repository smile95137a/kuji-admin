package com.group.admin.controller.admin;

import com.group.admin.constants.ApiPaths;
import com.group.admin.req.auth.AdminLoginReq;
import com.group.admin.req.auth.ChangePasswordReq;
import com.group.admin.req.auth.RefreshTokenReq;
import com.group.admin.res.auth.LoginRes;
import com.group.admin.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 後台認證控制器
 * 
 * <p>提供後台帳號登入、登出、密碼修改、Token 刷新等功能</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.ADMIN_AUTH)
@Tag(name = "後台認證", description = "後台帳號登入、登出、密碼管理 API")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 後台登入
     * 
     * <p>驗證帳號密碼，成功後回傳 JWT Token</p>
     * 
     * @param req 登入請求
     * @return 登入回應（Token 與使用者資訊）
     */
    @PostMapping("/login")
    @Operation(summary = "後台登入", description = "驗證帳號密碼，回傳 Access Token 及 Refresh Token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登入成功"),
        @ApiResponse(responseCode = "401", description = "帳號或密碼錯誤"),
        @ApiResponse(responseCode = "403", description = "帳號已停用")
    })
    public ResponseEntity<LoginRes> login(@Valid @RequestBody AdminLoginReq req) {
        log.info("後台登入請求：username={}", req.getUsername());
        LoginRes res = adminAuthService.login(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 首次登入修改密碼
     * 
     * <p>新帳號首次登入必須修改密碼</p>
     * 
     * @param req 修改密碼請求
     * @return 新的登入回應（含新 Token）
     */
    @PostMapping("/first-login/change-password")
    @Operation(summary = "首次登入修改密碼", description = "新帳號首次登入必須修改密碼，修改成功後回傳新 Token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "密碼修改成功"),
        @ApiResponse(responseCode = "400", description = "舊密碼錯誤或非首次登入"),
        @ApiResponse(responseCode = "401", description = "未認證")
    })
    public ResponseEntity<LoginRes> firstLoginChangePassword(@Valid @RequestBody ChangePasswordReq req) {
        log.info("首次登入修改密碼請求");
        LoginRes res = adminAuthService.firstLoginChangePassword(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 修改密碼
     * 
     * @param req 修改密碼請求
     * @return 無內容（成功）
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密碼", description = "已登入使用者修改自己的密碼")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "密碼修改成功"),
        @ApiResponse(responseCode = "400", description = "舊密碼錯誤"),
        @ApiResponse(responseCode = "401", description = "未認證")
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordReq req) {
        log.info("修改密碼請求");
        adminAuthService.changePassword(req);
        return ResponseEntity.ok().build();
    }

    /**
     * 刷新 Token
     * 
     * @param req 刷新 Token 請求
     * @return 新的登入回應（含新 Token）
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 取得新的 Access Token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "刷新成功"),
        @ApiResponse(responseCode = "401", description = "Refresh Token 無效或已過期")
    })
    public ResponseEntity<LoginRes> refreshToken(@Valid @RequestBody RefreshTokenReq req) {
        log.info("刷新 Token 請求");
        LoginRes res = adminAuthService.refreshToken(req);
        return ResponseEntity.ok(res);
    }

    /**
     * 登出
     * 
     * @return 無內容（成功）
     */
    @PostMapping("/logout")
    @Operation(summary = "登出", description = "登出當前使用者")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登出成功")
    })
    public ResponseEntity<Void> logout() {
        log.info("登出請求");
        adminAuthService.logout();
        return ResponseEntity.ok().build();
    }
}
