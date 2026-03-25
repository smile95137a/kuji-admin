package com.group.admin.res.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailRes {

    private String id;
    private String orderNo;
    private String userId;
    private String userNickname;
    private String userEmail;
    private String storeId;
    private String storeName;
    private String storeLogoUrl;
    private Integer totalItems;
    private String shippingMethod;
    private String shippingMethodName;
    private String shippingStatus;
    private String shippingStatusName;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String storeCode;
    private String storeName2;
    private String storeAddress;
    private String trackingNo;
    private String remark;
    private List<OrderItemRes> items;
    private List<StatusLogRes> statusHistory;
    private Long subtotal;
    private Long shippingFee;
    private Long discount;
    private Long totalAmount;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private String cancelReason;
}