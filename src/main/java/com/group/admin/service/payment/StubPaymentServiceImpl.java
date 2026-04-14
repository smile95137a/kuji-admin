package com.group.admin.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 金流 Stub 實作 — 自動標記支付成功，不真正扣款。
 * 未來替換為萬事達真實金流時注入此介面的正式實作即可。
 */
@Slf4j
@Service
public class StubPaymentServiceImpl implements PaymentGatewayService {

    @Override
    public PaymentResult processPayment(String orderId, Long amount, String paymentMethod) {
        String txId = UUID.randomUUID().toString();
        log.info("[STUB] 金流支付 {} 元，orderId={}，method={}，txId={}", amount, orderId, paymentMethod, txId);
        return PaymentResult.builder()
                .success(true)
                .transactionId(txId)
                .amount(amount)
                .message("[STUB] 金流支付成功")
                .build();
    }

    @Override
    public PaymentResult queryPayment(String transactionId) {
        log.info("[STUB] 查詢金流交易：txId={}", transactionId);
        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .message("[STUB] 交易狀態：成功")
                .build();
    }

    @Override
    public PaymentResult refundPayment(String transactionId, Long amount) {
        log.info("[STUB] 退款 {} 元，txId={}", amount, transactionId);
        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .amount(amount)
                .message("[STUB] 退款成功")
                .build();
    }
}
