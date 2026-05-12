package com.group.admin.gateway;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 出貨運費付款請求
 */
@Data
@Builder
public class ShippingPaymentRequest {

    /** GoMyPay 商戶訂單號（聚合付款用） */
    private String merchantOrderNo;

    /** 系統訂單編號（唯一，傳給 GoMyPay） */
    private String orderNumber;

    /** 訂單 ID（系統內部使用，callback 對帳用） */
    private String orderId;

    /** 付款金額（元） */
    private BigDecimal amount;

    /** 購買人姓名 */
    private String buyerName;

    /** 購買人 Email */
    private String buyerEmail;

    /** 購買人電話 */
    private String buyerPhone;

    /** 商品描述（顯示在 GoMyPay 付款頁） */
    private String itemDescription;

    /** 付款方式：CREDIT_CARD / BANK_TRANSFER */
    private String paymentMethod;
}
