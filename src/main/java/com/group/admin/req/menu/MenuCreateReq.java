package com.group.admin.req.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 選單建立請求 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "選單建立請求")
public class MenuCreateReq {

    /**
     * 選單名稱
     */
    @NotBlank(message = "選單名稱不可為空")
    @Size(max = 50, message = "選單名稱最多50字")
    @Schema(description = "選單名稱", example = "商品管理", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 選單代碼（唯一識別碼）
     */
    @NotBlank(message = "選單代碼不可為空")
    @Size(max = 50, message = "選單代碼最多50字")
    @Schema(description = "選單代碼", example = "product_management", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    /**
     * 選單路徑
     */
    @Size(max = 200, message = "選單路徑最多200字")
    @Schema(description = "選單路徑", example = "/admin/products")
    private String path;

    /**
     * 父選單ID（為空表示一級選單）
     */
    @Schema(description = "父選單ID，為空表示一級選單", example = "550e8400-e29b-41d4-a716-446655440001")
    private String parentId;

    /**
     * 選單圖示
     */
    @Size(max = 100, message = "圖示名稱最多100字")
    @Schema(description = "選單圖示", example = "mdi-package-variant")
    private String icon;

    /**
     * 排序順序
     */
    @Schema(description = "排序順序，數字越小越前面", example = "1")
    private Integer orderNum;

    /**
     * 是否可見
     */
    @Schema(description = "是否可見", example = "true")
    private Boolean isVisible;
}
