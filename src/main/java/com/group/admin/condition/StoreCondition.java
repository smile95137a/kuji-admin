package com.group.admin.condition;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 店家查詢條件
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "店家查詢條件")
public class StoreCondition extends BaseCondition {

    /**
     * 店家名稱（模糊查詢）
     */
    @Schema(description = "店家名稱（模糊查詢）", example = "KUJI")
    private String storeName;

    /**
     * 狀態
     */
    @Schema(description = "狀態（ACTIVE/INACTIVE）", example = "ACTIVE")
    private String status;

    /**
     * 店家負責人 ID（後端自動帶入，前端不用傳）
     */
    @Schema(description = "店家負責人 ID（後端自動帶入）", hidden = true)
    private String ownerId;
}
