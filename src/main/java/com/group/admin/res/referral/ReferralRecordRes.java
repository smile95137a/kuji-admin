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
     * 被推薦人顯示名稱
     */
    @Schema(description = "被推薦人顯示名稱")
    private String refereeUsername;
    
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
     * 回饋紅利
     */
    @Schema(description = "回饋紅利")
    private Long rewardBonus;

    /**
     * 是否已發放獎勵
     */
    @Schema(description = "是否已發放獎勵")
    private Boolean isRewardGiven;

    /**
     * 獎勵發放時間
     */
    @Schema(description = "獎勵發放時間")
    private LocalDateTime rewardGivenAt;
    
    /**
     * 推薦時間
     */
    @Schema(description = "推薦時間")
    private LocalDateTime referredAt;

    /**
     * 建立時間
     */
    @Schema(description = "建立時間")
    private LocalDateTime createdAt;
}
