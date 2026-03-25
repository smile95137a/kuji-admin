package com.group.admin.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 選單權限樹響應 DTO
 *
 * <p>用於前端動態渲染選單，包含每個選單節點的權限旗標。</p>
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "選單權限樹響應")
public class MenuPermissionRes {

    @Schema(description = "選單ID")
    private String id;

    @Schema(description = "選單名稱")
    private String name;

    @Schema(description = "選單代碼")
    private String code;

    @Schema(description = "選單路徑")
    private String path;

    @Schema(description = "父選單ID")
    private String parentId;

    @Schema(description = "選單圖示")
    private String icon;

    @Schema(description = "排序順序")
    private Integer orderNum;

    @Schema(description = "是否可查看")
    private Boolean canView;

    @Schema(description = "是否可編輯")
    private Boolean canEdit;

    @Schema(description = "是否可刪除")
    private Boolean canDelete;

    @Schema(description = "子選單列表")
    private List<MenuPermissionRes> children;
}
