package com.group.admin.res.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推薦碼回應
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "推薦碼回應")
public class ReferralCodeRes {
    
    /**
     * 主鍵 ID
     */
    @Schema(description = "推薦碼 ID")
    private String id;
    
    /**
     * 推薦碼
     */
    @Schema(description = "推薦碼")
    private String code;
    
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
     * 描述
     */
    @Schema(description = "推薦碼描述")
    private String description;
    
    /**
     * 是否啟用
     */
    @Schema(description = "是否啟用")
    private Boolean isActive;
    
    /**
     * 使用次數
     */
    @Schema(description = "使用次數")
    private Integer usedCount;
    
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
}
