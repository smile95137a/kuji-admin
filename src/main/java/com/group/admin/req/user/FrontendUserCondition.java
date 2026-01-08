package com.group.admin.req.user;

import com.group.admin.req.common.BaseCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 前台會員查詢條件
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "前台會員查詢條件")
public class FrontendUserCondition extends BaseCondition {
    
    @Schema(description = "Email（模糊查詢）")
    private String email;
    
    @Schema(description = "暱稱（模糊查詢）")
    private String nickname;
    
    @Schema(description = "狀態（ACTIVE/INACTIVE/SUSPENDED）")
    private String status;
    
    @Schema(description = "登入方式（LOCAL/GOOGLE/FACEBOOK/LINE）")
    private String provider;
    
    @Schema(description = "金幣最小值")
    private Long goldCoinsMin;
    
    @Schema(description = "金幣最大值")
    private Long goldCoinsMax;
}
