package com.group.admin.res.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推薦記錄回應
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "推薦記錄回應")
public class ReferralRecordRes {
    
    /**
     * 主鍵 ID
     */
    @Schema(description = "記錄 ID")
    private String id;
    
    /**
     * 被推薦人 ID
     */
    @Schema(description = "被推薦人 ID")
    private String userId;
    
    /**
     * 被推薦人名稱
     */
    @Schema(description = "被推薦人名稱")
    private String userName;
    
    /**
     * 推薦碼 ID
     */
    @Schema(description = "推薦碼 ID")
    private String referralCodeId;
    
    /**
     * 使用的推薦碼
     */
    @Schema(description = "使用的推薦碼")
    private String usedCode;
    
    /**
     * 所屬店家 ID
     */
    @Schema(description = "所屬店家 ID")
    private String storeId;
    
    /**
     * 店家名稱
     */
    @Schema(description = "店家名稱")
    private String storeName;
    
    /**
     * 推薦時間
     */
    @Schema(description = "推薦時間")
    private LocalDateTime referredAt;
}
