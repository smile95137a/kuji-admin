package com.group.admin.req.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘記密碼請求
 */
@Data
@Schema(description = "忘記密碼請求")
public class ForgotPasswordReq {
    
    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "註冊的 Email", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
}
