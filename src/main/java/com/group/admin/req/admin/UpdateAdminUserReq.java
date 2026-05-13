package com.group.admin.req.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新後台帳號請求")
public class UpdateAdminUserReq {

    @Schema(description = "顯示名稱", example = "店家小編 A")
    private String displayName;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "電話", example = "0912345678")
    private String phone;

    @Schema(description = "備註")
    private String remark;

    @Schema(description = "帳號狀態（ACTIVE/INACTIVE/PENDING）", example = "ACTIVE")
    private String status;

    @Schema(description = "小編綁定店家 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String storeId;
}
