package com.group.admin.gateway;

import lombok.Builder;
import lombok.Data;

/**
 * 出貨運費付款初始化結果（含 GoMyPay 付款頁 URL）
 */
@Data
@Builder
public class ShippingPaymentResult {

    /** GoMyPay 付款頁 URL，前端重導至此讓使用者付款 */
    private String payUrl;

    /** GoMyPay 交易編號（用於後續對帳） */
    private String gatewayTradeNo;

    /** 是否成功建立付款單 */
    private boolean success;

    /** 失敗訊息（success=false 時才有值） */
    private String errorMessage;
}
