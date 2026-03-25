package com.group.admin.req.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "修改密碼請求")
public class ChangePasswordReq {

    @NotBlank(message = "舊密碼不可為空")
    @Schema(description = "目前密碼")
    private String currentPassword;

    @NotBlank(message = "新密碼不可為空")
    @Size(min = 8, message = "新密碼至少 8 個字元")
    @Schema(description = "新密碼（至少 8 字元）")
    private String newPassword;
}
