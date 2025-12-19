package com.group.admin.entity;

import lombok.Data;

@Data
public class OrderDetail {
    private Long id;
    private Integer bonusPointsEarned;
    private Long orderId;
    private Long productDetailId;
    private Integer quantity;
    private Integer resultItemId;
    private Long storeProductId;
    private java.math.BigDecimal totalPrice;
    private java.math.BigDecimal unitPrice;
    private String productDetailName;
    private Integer productId;
    private String resultStatus;
    private String storeProductName;
    private String billNumber;
}
