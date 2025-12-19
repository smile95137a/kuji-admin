package com.group.admin.res.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登入回應
 * 
 * <p>包含 Token 及基本使用者資訊</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登入回應")
public class LoginRes {

    /**
     * Access Token（用於 API 認證）
     */
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiIsInR...")
    private String accessToken;

    /**
     * Refresh Token（用於刷新 Access Token）
     */
    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR...")
    private String refreshToken;

    /**
     * Token 類型（固定為 Bearer）
     */
    @Schema(description = "Token 類型", example = "Bearer")
    private String tokenType;

    /**
     * Access Token 過期時間（秒）
     */
    @Schema(description = "Access Token 過期時間（秒）", example = "86400")
    private Long expiresIn;

    /**
     * 是否需要首次登入修改密碼
     */
    @Schema(description = "是否需要首次登入修改密碼")
    private Boolean forceChangePassword;

    /**
     * 使用者資訊
     */
    @Schema(description = "使用者資訊")
    private UserInfo user;

    /**
     * 使用者基本資訊
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "使用者基本資訊")
    public static class UserInfo {

        /**
         * 使用者 ID
         */
        @Schema(description = "使用者 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String id;

        /**
         * 帳號（Email）
         */
        @Schema(description = "帳號", example = "admin@kuji.com")
        private String username;

        /**
         * 顯示名稱
         */
        @Schema(description = "顯示名稱", example = "系統管理員")
        private String displayName;

        /**
         * 角色列表
         */
        @Schema(description = "角色列表", example = "[\"ROLE_ADMIN\"]")
        private List<String> roles;
    }
}
