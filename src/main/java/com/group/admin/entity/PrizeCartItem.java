package com.group.admin.entity;

import lombok.Data;

@Data
public class PrizeCartItem {
    private Long prizeCartItemId;
    private Long cartId;
    private Boolean isSelected;
    private Long productDetailId;
    private Integer quantity;
    private java.math.BigDecimal size;
    private java.math.BigDecimal sliverPrice;
}
