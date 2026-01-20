package com.group.admin.req.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重設密碼請求
 */
@Data
@Schema(description = "重設密碼請求")
public class ResetPasswordReq {
    
    @NotBlank(message = "重設 token 不可為空")
    @Schema(description = "重設 token（從郵件連結取得）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
    
    @NotBlank(message = "新密碼不可為空")
    @Size(min = 8, max = 20, message = "密碼長度必須在 8-20 字元之間")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "密碼必須包含大小寫字母和數字"
    )
    @Schema(description = "新密碼（8-20字元，需包含大小寫字母和數字）", example = "NewPass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
    
    @NotBlank(message = "確認密碼不可為空")
    @Schema(description = "確認新密碼", example = "NewPass123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}
