package com.group.admin.req.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "套用推薦碼請求")
public class ApplyReferralReq {

    @NotBlank(message = "推薦碼不能為空")
    @Schema(description = "推薦碼", example = "STORE001")
    private String code;
}
