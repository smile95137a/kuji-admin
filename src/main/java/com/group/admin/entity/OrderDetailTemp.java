package com.group.admin.entity;

import lombok.Data;

@Data
public class OrderDetailTemp {
    private Long id;
    private Integer bonusPointsEarned;
    private Long orderId;
    private Long productDetailId;
    private Integer quantity;
    private Integer resultItemId;
    private Long storeProductId;
    private java.math.BigDecimal totalPrice;
    private java.math.BigDecimal unitPrice;
}
