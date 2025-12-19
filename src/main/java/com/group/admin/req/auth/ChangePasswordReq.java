package com.group.admin.req.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密碼請求
 * 
 * <p>用於首次登入強制修改密碼或一般修改密碼</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Data
@Schema(description = "修改密碼請求")
public class ChangePasswordReq {

    /**
     * 舊密碼
     */
    @NotBlank(message = "舊密碼不可為空")
    @Schema(description = "舊密碼", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPassword;

    /**
     * 新密碼
     */
    @NotBlank(message = "新密碼不可為空")
    @Size(min = 8, message = "密碼長度至少 8 位")
    @Schema(description = "新密碼（至少 8 位，包含大小寫字母與數字）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    /**
     * 確認新密碼
     */
    @NotBlank(message = "確認密碼不可為空")
    @Schema(description = "確認新密碼", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}
