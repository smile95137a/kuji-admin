package com.group.admin.entity;

import lombok.Data;

@Data
public class OrderTemp {
    private Integer id;
    private String opmode;
    private String billingAddress;
    private String billingArea;
    private String billingCity;
    private String billingEmail;
    private String billingName;
    private String billingPhone;
    private String billingZipCode;
    private Integer bonusPointsEarned;
    private Integer bonusPointsUsed;
    private String cartItemId;
    private java.time.LocalDateTime createdAt;
    private String express;
    private String invoice;
    private Boolean isFreeShipping;
    private String orderNumber;
    private java.time.LocalDateTime paidAt;
    private String paymentMethod;
    private String resultStatus;
    private String shippingAddress;
    private String shippingArea;
    private String shippingCity;
    private java.math.BigDecimal shippingCost;
    private String shippingEmail;
    private String shippingMethod;
    private String shippingName;
    private String shippingPhone;
    private String shippingZipCode;
    private String shopId;
    private java.math.BigDecimal totalAmount;
    private String trackingNumber;
    private java.time.LocalDateTime updatedAt;
    private Long userId;
}
