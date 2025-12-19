package com.group.admin.res.store;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店家列表項目回應
 * 
 * <p>用於列表顯示，只包含必要欄位</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "店家列表項目回應")
public class StoreListRes {

    /**
     * 店家 ID
     */
    @Schema(description = "店家 ID", example = "1")
    private Long id;

    /**
     * 店家名稱
     */
    @Schema(description = "店家名稱", example = "KUJI 官方商店")
    private String storeName;

    /**
     * 短描述
     */
    @Schema(description = "短描述", example = "專營一番賞、扭蛋精品")
    private String shortDescription;

    /**
     * Logo URL
     */
    @Schema(description = "Logo URL")
    private String logoUrl;

    /**
     * 狀態
     */
    @Schema(description = "狀態", example = "ACTIVE")
    private String status;

    /**
     * 狀態顯示名稱
     */
    @Schema(description = "狀態顯示名稱", example = "啟用")
    private String statusDisplayName;

    /**
     * 店家主帳號名稱
     */
    @Schema(description = "店家主帳號名稱", example = "王小明")
    private String ownerName;
}
