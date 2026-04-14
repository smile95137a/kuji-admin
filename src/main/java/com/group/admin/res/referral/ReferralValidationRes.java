package com.group.admin.res.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推薦碼驗證回應
 * 包含推薦碼的有效性與獎勵信息
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "推薦碼驗證結果", description = "推薦碼驗證結果與獎勵信息")
public class ReferralValidationRes {
    
    /**
     * 推薦碼是否有效
     */
    @Schema(title = "是否有效", description = "推薦碼是否有效", example = "true")
    private Boolean isValid;
    
    /**
     * 推薦碼的所有者（店家或推薦用戶）
     */
    @Schema(title = "推薦者", description = "推薦碼的所有者或店鋪名稱", example = "鬼滅之刃一番賞")
    private String referrerName;
    
    /**
     * 推薦店家 ID（如果是店家推薦碼）
     */
    @Schema(title = "店家 ID", description = "推薦店家的 ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String referredStoreId;
    
    /**
     * 獎勵金幣數
     */
    @Schema(title = "獎勵金幣", description = "新用戶將獲得的金幣數", example = "100")
    private Long rewardCoins;
    
    /**
     * 獎勵描述
     */
    @Schema(title = "獎勵描述", description = "獎勵的文字描述", example = "首次註冊完成，獲得 100 金幣")
    private String rewardDescription;
    
    /**
     * 錯誤信息（如果無效）
     */
    @Schema(title = "錯誤信息", description = "如果推薦碼無效，顯示錯誤理由", example = "推薦碼不存在或已過期")
    private String errorMessage;
}
