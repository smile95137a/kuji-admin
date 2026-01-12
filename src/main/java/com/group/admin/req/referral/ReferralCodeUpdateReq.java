package com.group.admin.req.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新推薦碼請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "更新推薦碼請求")
public class ReferralCodeUpdateReq {
    
    /**
     * 描述
     */
    @Size(max = 200, message = "描述不能超過 200 字元")
    @Schema(description = "推薦碼描述")
    private String description;
    
    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用")
    private Boolean isActive;
}
