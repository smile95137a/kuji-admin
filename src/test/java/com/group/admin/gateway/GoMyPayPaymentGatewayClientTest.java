package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
import com.group.admin.entity.RechargeOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoMyPayPaymentGatewayClient 測試")
class GoMyPayPaymentGatewayClientTest {

    @Test
    @DisplayName("儲值信用卡付款應回傳完整 payUrl 與表單欄位")
    void charge_ShouldReturnFullPayUrlAndFormFields() {
        GoMyPayPaymentGatewayClient client = new GoMyPayPaymentGatewayClient(buildProperties());
        RechargeOrder order = RechargeOrder.builder()
                .id("RC260514123456ABCDEF001")
                .priceTwd(new BigDecimal("100"))
                .buyerName("測試會員")
                .buyerPhone("0912345678")
                .buyerEmail("test@kuji.local")
                .build();

        GatewayInitResult result = client.charge(order, GoMyPaySupport.PAYMENT_METHOD_CREDIT_CARD);

        assertThat(result.payUrl())
                .startsWith("https://n.gomypay.asia/TestShuntClass.aspx?")
                .contains("Send_Type=0")
                .contains("Pay_Mode_No=2")
                .contains("Order_No=RC260514123456ABCDEF001")
                .contains("Callback_Url=")
                .contains("Return_url=")
                .contains("Str_Check=hash-key");
        assertThat(result.actionUrl()).isEqualTo("https://n.gomypay.asia/TestShuntClass.aspx");
        assertThat(result.submitMethod()).isEqualTo("POST");
        assertThat(result.formFields())
                .containsEntry("Send_Type", "0")
                .containsEntry("Pay_Mode_No", "2")
                .containsEntry("TransCode", "00")
                .containsEntry("TransMode", "1")
                .containsEntry("Installment", "0")
                .containsEntry("Callback_Url", "https://api.kuji.local/api/wallet/recharge/callback")
                .containsEntry("Return_url", "https://client.kuji.local/client/member-center/deposit-payment-result");
    }

    private GoMyPayProperties buildProperties() {
        GoMyPayProperties properties = new GoMyPayProperties();
        properties.setApiUrl("https://n.gomypay.asia/TestShuntClass.aspx");
        properties.setShopId("encrypted-shop-id");
        properties.setVerifyCustomerId("60530393");
        properties.setHashKey("hash-key");
        properties.setRechargeReturnUrl("https://client.kuji.local/client/member-center/deposit-payment-result");
        properties.setRechargeNotifyUrl("https://api.kuji.local/api/wallet/recharge/callback");
        return properties;
    }
}
