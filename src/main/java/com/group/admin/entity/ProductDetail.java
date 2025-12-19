package com.group.admin.entity;

import lombok.Data;

@Data
public class ProductDetail {
    private Long productDetailId;
    private java.time.LocalDateTime createDate;
    private String description;
    private String drawnNumbers;
    private String grade;
    private java.math.BigDecimal height;
    private String imageUrls;
    private java.math.BigDecimal length;
    private String material;
    private String note;
    private java.math.BigDecimal price;
    private String prizeNumber;
    private Long productId;
    private String productName;
    private Integer quantity;
    private String rarity;
    private String size;
    private java.math.BigDecimal sliverPrice;
    private String specification;
    private Integer stockQuantity;
    private java.time.LocalDateTime updateDate;
    private java.math.BigDecimal width;
    private Double probability;
    private String isPrize;
}
