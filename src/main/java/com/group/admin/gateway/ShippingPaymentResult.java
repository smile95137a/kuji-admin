package com.group.admin.gateway;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 出貨運費付款初始化結果。
 */
@Data
@Builder
public class ShippingPaymentResult {

    /** GoMyPay 託管付款頁 URL，信用卡會由前端送出表單導向此頁。 */
    private String payUrl;

    private String submitMethod;

    private String actionUrl;

    private Map<String, String> formFields;

    /** GoMyPay 或系統端付款群組交易編號。 */
    private String gatewayTradeNo;

    private String gatewayResult;

    private String retMsg;

    private String virtualAccount;

    private String payInfo;

    private String limitDate;

    private String rawPayload;

    /** 是否成功建立付款資訊。 */
    private boolean success;

    /** success=false 時的錯誤原因。 */
    private String errorMessage;
}
