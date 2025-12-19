package com.group.admin.res.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 後台使用者資訊回應
 * 
 * <p>包含完整的使用者資訊（不含密碼）</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "後台使用者資訊回應")
public class AdminUserRes {

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
     * Email
     */
    @Schema(description = "Email", example = "admin@kuji.com")
    private String email;

    /**
     * 顯示名稱
     */
    @Schema(description = "顯示名稱", example = "系統管理員")
    private String displayName;

    /**
     * 聯絡電話
     */
    @Schema(description = "聯絡電話", example = "0912345678")
    private String phone;

    /**
     * 帳號狀態
     */
    @Schema(description = "帳號狀態", example = "ACTIVE")
    private String status;

    /**
     * 帳號狀態顯示名稱
     */
    @Schema(description = "帳號狀態顯示名稱", example = "啟用")
    private String statusDisplayName;

    /**
     * 是否需要首次登入改密碼
     */
    @Schema(description = "是否需要首次登入改密碼")
    private Boolean forceChangePassword;

    /**
     * 角色列表
     */
    @Schema(description = "角色列表")
    private List<RoleInfo> roles;

    /**
     * 所屬店家資訊列表（StoreOwner/StoreEditor 才有）
     */
    @Schema(description = "所屬店家資訊列表")
    private List<StoreInfo> stores;

    /**
     * 最後登入時間
     */
    @Schema(description = "最後登入時間")
    private LocalDateTime lastLoginAt;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 備註
     */
    @Schema(description = "備註")
    private String remark;

    /**
     * 角色資訊
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色資訊")
    public static class RoleInfo {

        /**
         * 角色 ID
         */
        @Schema(description = "角色 ID", example = "550e8400-e29b-41d4-a716-446655440001")
        private String id;

        /**
         * 角色代碼
         */
        @Schema(description = "角色代碼", example = "ROLE_ADMIN")
        private String code;

        /**
         * 角色名稱
         */
        @Schema(description = "角色名稱", example = "Admin")
        private String name;
    }

    /**
     * 店家資訊
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "店家資訊")
    public static class StoreInfo {

        /**
         * 店家 ID
         */
        @Schema(description = "店家 ID", example = "550e8400-e29b-41d4-a716-446655440002")
        private String id;

        /**
         * 店家名稱
         */
        @Schema(description = "店家名稱", example = "KUJI 官方商店")
        private String storeName;

        /**
         * 角色類型（OWNER/EDITOR）
         */
        @Schema(description = "角色類型", example = "OWNER")
        private String roleType;
    }
}
