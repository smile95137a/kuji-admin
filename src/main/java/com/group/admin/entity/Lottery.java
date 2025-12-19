package com.group.admin.entity;

import lombok.Data;

@Data
public class Lottery {
    private String id;
    private String storeId;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private String subCategory;
    private Long pricePerDraw;
    private Long discountedPrice;
    private Integer autoDiscountEnabled;
    private Integer allowMultiDraw;
    private String multiDrawOptions;
    private java.time.LocalDateTime scheduledAt;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
    private Integer totalDraws;
    private Integer maxDraws;
    private String status;
    private Integer orderNum;
    private Integer weight;
    private String createdBy;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String remark;
}
