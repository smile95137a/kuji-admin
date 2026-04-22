package com.group.admin.res.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 訂單回應（列表用）
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRes {
    
    /**
     * 訂單 ID
     */
    private String id;
    
    /**
     * 訂單編號
     */
    private String orderNo;
    
    /**
     * 玩家 ID
     */
    private String userId;
    
    /**
     * 玩家暱稱
     */
    private String userNickname;
    
    /**
     * 玩家 Email
     */
    private String userEmail;
    
    /**
     * 店家 ID
     */
    private String storeId;
    
    /**
     * 店家名稱
     */
    private String storeName;
    
    /**
     * 商品總數
     */
    private Integer totalItems;
    
    /**
     * 配送方式代碼
     */
    private String shippingMethod;
    
    /**
     * 配送方式名稱
     */
    private String shippingMethodName;
    
    /**
     * 訂單狀態代碼
     */
    private String shippingStatus;
    
    /**
     * 訂單狀態名稱
     */
    private String shippingStatusName;
    
    /**
     * 收件人姓名
     */
    private String recipientName;
    
    /**
     * 收件人電話
     */
    private String recipientPhone;
    
    /**
     * 物流單號
     */
    private String trackingNo;
    
    /**
     * 總金額（元）
     */
    private Long totalAmount;

    /**
     * 運費（元）
     */
    private Long shippingFee;

    /**
     * 付款狀態（PAYMENT_PENDING/PAID/FAILED）
     */
    private String paymentStatus;
    
    /**
     * 付款方式（GOLD/BONUS）
     */
    private String paymentMethod;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 出貨時間
     */
    private LocalDateTime shippedAt;
    
    /**
     * 完成時間
     */
    private LocalDateTime completedAt;
}
