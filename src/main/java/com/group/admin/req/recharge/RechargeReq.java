package com.group.admin.req.recharge;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 前台使用者儲值請求
 * 
 * @author Kuji Admin
 * @since 2026-02-08
 */
@Data
public class RechargeReq {
    
    /**
     * 儲值方案 ID（必填）
     */
    @NotBlank(message = "儲值方案 ID 不可為空")
    private String planId;
    
    /**
     * 支付方式（選填，預設 GOMYPAY）
     * 目前支援：GOMYPAY（信用卡／行動支付，由 GoMyPay 金流處理）
     */
    private String paymentMethod = "GOMYPAY";
    
    /**
     * 支付備註（選填）
     * 例：用戶可輸入相關備註信息
     */
    private String remark;
}
