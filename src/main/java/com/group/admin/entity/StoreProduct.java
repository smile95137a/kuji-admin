package com.group.admin.entity;

import lombok.Data;

@Data
public class StoreProduct {
    private Long storeProductId;
    private String categoryId;
    private java.time.LocalDateTime createdAt;
    private Long createdUserId;
    private String description;
    private String details;
    private java.math.BigDecimal height;
    private String imageUrls;
    private Boolean isSpecialPrice;
    private java.math.BigDecimal length;
    private Integer popularity;
    private java.math.BigDecimal price;
    private String productCode;
    private String productName;
    private String shippingMethod;
    private java.math.BigDecimal shippingPrice;
    private java.math.BigDecimal size;
    private Integer soldQuantity;
    private java.math.BigDecimal specialPrice;
    private String specification;
    private String status;
    private Integer stockQuantity;
    private Long updateUserId;
    private java.time.LocalDateTime updatedAt;
    private java.math.BigDecimal width;
    private Long createdUser;
    private Long updateUser;
}
