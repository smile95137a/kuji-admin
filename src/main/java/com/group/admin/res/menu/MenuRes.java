package com.group.admin.res.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 選單響應 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "選單響應")
public class MenuRes {

    /**
     * 選單ID
     */
    @Schema(description = "選單ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    /**
     * 選單名稱
     */
    @Schema(description = "選單名稱", example = "商品管理")
    private String name;

    /**
     * 選單代碼
     */
    @Schema(description = "選單代碼", example = "product_management")
    private String code;

    /**
     * 選單路徑
     */
    @Schema(description = "選單路徑", example = "/admin/products")
    private String path;

    /**
     * 父選單ID
     */
    @Schema(description = "父選單ID，為空表示一級選單", example = "550e8400-e29b-41d4-a716-446655440001")
    private String parentId;

    /**
     * 選單圖示
     */
    @Schema(description = "選單圖示", example = "mdi-package-variant")
    private String icon;

    /**
     * 排序順序
     */
    @Schema(description = "排序順序", example = "1")
    private Integer orderNum;

    /**
     * 是否可見
     */
    @Schema(description = "是否可見", example = "true")
    private Boolean isVisible;

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
     * 子選單列表
     */
    @Schema(description = "子選單列表")
    private List<MenuRes> children;
}
