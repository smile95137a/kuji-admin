package com.group.admin.req.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新後台使用者請求")
public class UpdateAdminUserReq {

    @Schema(description = "顯示名稱", example = "王小明")
    private String displayName;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "聯絡電話", example = "0912345678")
    private String phone;

    @Schema(description = "帳號狀態（ACTIVE/INACTIVE/PENDING）", example = "ACTIVE")
    private String status;
}
