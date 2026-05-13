package com.group.admin.req.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
@Schema(description = "重新發送 Email 驗證信請求")
public class ResendVerificationReq {

    @Email(message = "Email 格式錯誤")
    @Schema(description = "欲重寄驗證信的會員 Email", example = "member@example.com")
    private String email;
}
