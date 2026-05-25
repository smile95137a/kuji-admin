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
     * 指定店家 ID。未傳入時維持原店家；若要更換店家，後端會檢查該店家是否已有推薦碼。
     */
    @Schema(description = "指定店家 ID")
    private String storeId;
    
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
