package com.group.admin.gateway;

import lombok.Builder;
import lombok.Data;

/**
 * GoMyPay 非同步通知解析結果
 */
@Data
@Builder
public class ShippingCallbackResult {

    /** 系統訂單號（GoMyPay 傳回的 Order_No） */
    private String orderNumber;

    /** GoMyPay 交易編號 */
    private String gatewayTradeNo;

    /** 付款是否成功 */
    private boolean success;

    /** 失敗原因（success=false 時才有值） */
    private String errorMessage;

    /** 原始 callback 內容（供 log 用） */
    private String rawPayload;
}
