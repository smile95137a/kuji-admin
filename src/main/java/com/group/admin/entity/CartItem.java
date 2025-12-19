package com.group.admin.entity;

import lombok.Data;

@Data
public class CartItem {
    private Long cartItemId;
    private Long cartId;
    private Boolean isSelected;
    private Integer quantity;
    private java.math.BigDecimal size;
    private Long storeProductId;
    private java.math.BigDecimal totalPrice;
    private java.math.BigDecimal unitPrice;
    private Long productDetailId;
    private String status;
}
