package com.group.admin.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 角色含權限明細響應 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色含權限明細響應")
public class RoleWithPermissionsRes {

    @Schema(description = "角色ID")
    private String id;

    @Schema(description = "角色名稱")
    private String name;

    @Schema(description = "角色代碼")
    private String code;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "選單權限列表")
    private List<MenuPermissionItem> menuPermissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "選單權限項目")
    public static class MenuPermissionItem {

        @Schema(description = "選單ID")
        private String menuId;

        @Schema(description = "選單名稱")
        private String menuName;

        @Schema(description = "選單代碼")
        private String menuCode;

        @Schema(description = "是否可查看")
        private Boolean canView;

        @Schema(description = "是否可編輯")
        private Boolean canEdit;

        @Schema(description = "是否可刪除")
        private Boolean canDelete;
    }
}
