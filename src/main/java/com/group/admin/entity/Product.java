package com.group.admin.entity;

import lombok.Data;

@Data
public class Product {
    private Integer productId;
    private java.math.BigDecimal bonusPrice;
    private java.time.LocalDateTime createdAt;
    private Integer createdUser;
    private String description;
    private java.time.LocalDateTime endDate;
    private java.math.BigDecimal height;
    private String imageUrls;
    private java.math.BigDecimal length;
    private java.math.BigDecimal price;
    private String prizeCategory;
    private String productName;
    private String productType;
    private String rarity;
    private java.math.BigDecimal size;
    private java.math.BigDecimal sliverPrice;
    private String specification;
    private java.time.LocalDateTime startDate;
    private String status;
    private Integer stockQuantity;
    private Integer updateUser;
    private java.time.LocalDateTime updatedAt;
    private java.math.BigDecimal width;
    private Long categoryId;
    private String bannerImageUrl;
}
