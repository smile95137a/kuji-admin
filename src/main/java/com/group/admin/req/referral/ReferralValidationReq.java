package com.group.admin.req.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推薦碼驗證請求
 * 用於 POST /api/auth/validate-referral 端點
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "推薦碼驗證", description = "驗證推薦碼是否有效並取得獎勵信息")
public class ReferralValidationReq {
    
    /**
     * 推薦碼
     * 格式：通常為 6-50 個字符的唯一代碼
     * 例如：STORE123456 或 KUJI_2024
     */
    @NotBlank(message = "推薦碼不能為空")
    @Size(min = 6, max = 50, message = "推薦碼長度應在 6-50 字符之間")
    @Schema(
        title = "推薦碼",
        description = "推薦碼 (6-50 字符)",
        example = "STORE123456",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String referralCode;
}
