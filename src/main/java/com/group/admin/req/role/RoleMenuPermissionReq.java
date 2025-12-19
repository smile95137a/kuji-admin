package com.group.admin.req.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 角色選單權限設定請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "角色選單權限設定請求")
public class RoleMenuPermissionReq {

    /**
     * 角色ID
     */
    @NotNull(message = "角色ID不可為空")
    @Schema(description = "角色ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleId;

    /**
     * 權限列表
     */
    @Schema(description = "權限列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MenuPermission> permissions;

    /**
     * 選單權限項目
     */
    @Data
    @Schema(description = "選單權限項目")
    public static class MenuPermission {

        /**
         * 選單ID
         */
        @NotNull(message = "選單ID不可為空")
        @Schema(description = "選單ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        private String menuId;

        /**
         * 是否可查看
         */
        @Schema(description = "是否可查看", example = "true")
        private Boolean canView;

        /**
         * 是否可編輯
         */
        @Schema(description = "是否可編輯", example = "true")
        private Boolean canEdit;

        /**
         * 是否可刪除
         */
        @Schema(description = "是否可刪除", example = "false")
        private Boolean canDelete;
    }
}
