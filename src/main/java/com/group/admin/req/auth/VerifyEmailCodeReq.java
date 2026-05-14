package com.group.admin.req.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Email 驗證碼驗證請求")
public class VerifyEmailCodeReq {

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "待驗證 Email", example = "member@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "驗證碼不可為空")
    @Pattern(regexp = "^\\d{6}$", message = "驗證碼必須為 6 位數字")
    @Schema(description = "6 位數驗證碼", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
