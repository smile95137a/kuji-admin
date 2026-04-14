package com.group.admin.req.referral;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 推薦碼驗證請求（前台公開端點使用）
 */
@Data
public class ReferralValidateReq {

    @NotBlank(message = "請輸入推薦碼")
    @Size(max = 20)
    private String code;
}
