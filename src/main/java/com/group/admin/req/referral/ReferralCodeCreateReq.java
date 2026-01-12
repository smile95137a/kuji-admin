package com.group.admin.req.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 建立推薦碼請求
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "建立推薦碼請求")
public class ReferralCodeCreateReq {
    
    /**
     * 推薦碼（6-20 字元，英數混合）
     */
    @NotBlank(message = "推薦碼不能為空")
    @Size(min = 6, max = 20, message = "推薦碼長度必須在 6-20 字元之間")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "推薦碼只能包含英文字母和數字")
    @Schema(description = "推薦碼", example = "STORE001")
    private String code;
    
    /**
     * 所屬店家 ID
     */
    @NotBlank(message = "店家 ID 不能為空")
    @Schema(description = "所屬店家 ID")
    private String storeId;
    
    /**
     * 描述
     */
    @Size(max = 200, message = "描述不能超過 200 字元")
    @Schema(description = "推薦碼描述", example = "店家專屬推薦碼")
    private String description;
}
