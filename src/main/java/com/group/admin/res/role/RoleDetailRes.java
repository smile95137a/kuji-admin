package com.group.admin.res.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色詳情響應 DTO（包含權限）
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "角色詳情響應（包含權限）")
public class RoleDetailRes {

    /**
     * 角色ID
     */
    @Schema(description = "角色ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 角色名稱
     */
    @Schema(description = "角色名稱", example = "店家管理員")
    private String name;

    /**
     * 角色代碼
     */
    @Schema(description = "角色代碼", example = "STORE_OWNER")
    private String code;

    /**
     * 角色描述
     */
    @Schema(description = "角色描述", example = "店家擁有者，可管理自己店鋪的所有資料")
    private String description;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Schema(description = "更新時間")
    private LocalDateTime updatedAt;

    /**
     * 選單權限列表
     */
    @Schema(description = "選單權限列表")
    private List<MenuPermissionRes> menuPermissions;

    /**
     * 選單權限響應
     */
    @Data
    @Schema(description = "選單權限響應")
    public static class MenuPermissionRes {

        /**
         * 選單ID
         */
        @Schema(description = "選單ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String menuId;

        /**
         * 選單名稱
         */
        @Schema(description = "選單名稱", example = "商品管理")
        private String menuName;

        /**
         * 選單代碼
         */
        @Schema(description = "選單代碼", example = "product_management")
        private String menuCode;

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
