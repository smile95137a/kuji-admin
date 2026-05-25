package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
import com.group.admin.entity.RechargeOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GoMyPayGatewayClientTest {

    @Test
    void creditCardRechargeUsesHostedPageWithoutDirectCardFields() {
        GoMyPayPaymentGatewayClient client = new GoMyPayPaymentGatewayClient(properties(), null);
        RechargeOrder order = RechargeOrder.builder()
                .id("RC260525235959ABC")
                .priceTwd(BigDecimal.valueOf(1000))
                .buyerName("測試會員")
                .buyerPhone("0912345678")
                .buyerEmail("test@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        GatewayInitResult result = client.charge(order, GoMyPaySupport.PAYMENT_METHOD_CREDIT_CARD);

        assertThat(result.success()).isTrue();
        assertThat(result.submitMethod()).isEqualTo("POST");
        assertThat(result.actionUrl()).isEqualTo(properties().getApiUrl());
        assertThat(result.formFields())
                .containsEntry("Send_Type", "0")
                .containsEntry("Order_No", order.getId())
                .containsEntry("TransCode", "00")
                .containsEntry("TransMode", "1")
                .containsEntry("Installment", "0")
                .doesNotContainKeys("CardNo", "ExpireDate", "CVV");
    }

    @Test
    void creditCardShippingUsesHostedPageWithoutDirectCardFields() {
        GoMyPayShippingGatewayClient client = new GoMyPayShippingGatewayClient(properties(), null);
        ShippingPaymentRequest request = ShippingPaymentRequest.builder()
                .merchantOrderNo("SP260525235959ABC")
                .amount(BigDecimal.valueOf(120))
                .buyerName("測試會員")
                .buyerPhone("0912345678")
                .buyerEmail("test@example.com")
                .itemDescription("訂單運費")
                .paymentMethod(GoMyPaySupport.PAYMENT_METHOD_CREDIT_CARD)
                .build();

        ShippingPaymentResult result = client.createPayment(request);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSubmitMethod()).isEqualTo("POST");
        assertThat(result.getActionUrl()).isEqualTo(properties().getApiUrl());
        assertThat(result.getFormFields())
                .containsEntry("Send_Type", "0")
                .containsEntry("Order_No", request.getMerchantOrderNo())
                .containsEntry("TransCode", "00")
                .containsEntry("TransMode", "1")
                .containsEntry("Installment", "0")
                .doesNotContainKeys("CardNo", "ExpireDate", "CVV");
    }

    private GoMyPayProperties properties() {
        GoMyPayProperties properties = new GoMyPayProperties();
        properties.setApiUrl("https://n.gomypay.asia/TestShuntClass.aspx");
        properties.setShopId("TEST_CUSTOMER");
        properties.setTransactionPassword("test-password");
        properties.setRechargeReturnUrl("https://example.test/member/deposit-payment-result");
        properties.setRechargeNotifyUrl("https://example.test/api/wallet/recharge/callback");
        properties.setShippingReturnUrl("https://example.test/member/order-payment-result");
        properties.setShippingNotifyUrl("https://example.test/api/payment/shipping/callback");
        return properties;
    }
}
