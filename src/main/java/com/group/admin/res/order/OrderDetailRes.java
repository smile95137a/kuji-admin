package com.group.admin.res.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單詳情回應
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailRes {
    
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
     * 收件地址（宅配）
     */
    private String recipientAddress;
    
    /**
     * 超商店號（超商取貨）
     */
    private String storeCode;
    
    /**
     * 超商店名
     */
    private String storeName2;
    
    /**
     * 超商地址
     */
    private String storeAddress;
    
    /**
     * 物流單號
     */
    private String trackingNo;
    
    /**
     * 備註
     */
    private String remark;
    
    /**
     * 訂單項目列表
     */
    private List<OrderItemRes> items;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
    
    /**
     * 出貨時間
     */
    private LocalDateTime shippedAt;
    
    /**
     * 完成時間
     */
    private LocalDateTime completedAt;
    
    /**
     * 取消時間
     */
    private LocalDateTime cancelledAt;
    
    /**
     * 取消者 ID
     */
    private String cancelledBy;
    
    /**
     * 取消原因
     */
    private String cancelReason;
}
