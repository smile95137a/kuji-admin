package com.group.admin.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 更新角色權限請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "更新角色權限請求")
public class UpdateRolePermissionsReq {

    @NotNull(message = "選單權限列表不可為空")
    @Valid
    @Schema(description = "選單權限列表")
    private List<MenuPermissionItem> menuPermissions;

    @Data
    @Schema(description = "選單權限項目")
    public static class MenuPermissionItem {

        @NotBlank(message = "選單ID不可為空")
        @Schema(description = "選單ID")
        private String menuId;

        @Schema(description = "是否可查看", defaultValue = "false")
        private Boolean canView = false;

        @Schema(description = "是否可編輯", defaultValue = "false")
        private Boolean canEdit = false;

        @Schema(description = "是否可刪除", defaultValue = "false")
        private Boolean canDelete = false;
    }
}
