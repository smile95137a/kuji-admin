package com.group.admin.entity;

import lombok.Data;

@Data
public class Order {
    private Integer id;
    private String orderNo;
    private Long userId;
    private Long amount;
    private String orderType;
    private String paymentProvider;
    private String providerTradeNo;
    private String status;
    private String remark;
    private java.time.LocalDateTime paidAt;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
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
    private String invoice;
    private Boolean isFreeShipping;
    private String orderNumber;
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
    private java.math.BigDecimal totalAmount;
    private String trackingNumber;
    private String opmode;
    private String express;
    private String shopId;
    private String billNumber;
    private String ePayaccount;
    private String donationCode;
    private String state;
    private String type;
    private String shippingMethodId;
    private String shippingMehtodId;
    private String shopAddress;
    private String shopName;
    private String vehicle;
    private String uncode;
}
