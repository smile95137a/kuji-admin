package com.group.admin.service.payment;

public interface PaymentGatewayService {

    PaymentResult processPayment(String orderId, Long amount, String paymentMethod);

    PaymentResult queryPayment(String transactionId);

    PaymentResult refundPayment(String transactionId, Long amount);
}
