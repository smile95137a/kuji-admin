package com.group.admin.res.referral;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 推薦碼驗證回應（前台公開端點使用）
 */
@Data
@AllArgsConstructor
public class ReferralValidateRes {

    private boolean valid;
    private String code;
    private String storeName;
}
