package com.group.admin.gateway;

import java.util.Map;

/**
 * 出貨運費金流介面
 * 實作：GoMyPayShippingGatewayClient（正式）
 */
public interface ShippingPaymentGatewayClient {

    /**
     * 建立付款單，取得 GoMyPay 付款頁 URL
     */
    ShippingPaymentResult createPayment(ShippingPaymentRequest request);

    /**
     * 驗證並解析 GoMyPay 非同步通知（notify_url），回傳訂單號與結果
     *
     * @param params GoMyPay 傳來的 POST 參數
     * @return 解析結果；success=true 代表付款成功
     */
    ShippingCallbackResult parseCallback(Map<String, String> params);
}
