package com.group.admin.req.recharge;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 儲值方案查詢條件
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "儲值方案查詢條件")
public class RechargePlanCondition extends BaseCondition {
    
    /**
     * 方案名稱（模糊查詢）
     */
    @Schema(description = "方案名稱", example = "超值")
    private String name;
    
    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用", example = "true")
    private Boolean isActive;
    
    /**
     * 最低金額
     */
    @Schema(description = "最低金額", example = "100")
    private Long amountMin;
    
    /**
     * 最高金額
     */
    @Schema(description = "最高金額", example = "1000")
    private Long amountMax;
}
