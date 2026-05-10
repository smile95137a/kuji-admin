package com.group.admin.req.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新本人資料請求")
public class UpdateMyProfileReq {

    @Schema(description = "顯示名稱", example = "王小明")
    private String displayName;

    @Schema(description = "聯絡電話", example = "0912345678")
    private String phone;
}
