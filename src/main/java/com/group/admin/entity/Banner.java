package com.group.admin.entity;

import lombok.Data;

@Data
public class Banner {
    private String id;
    private String storeId;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private Integer orderNum;
    private String status;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private Long bannerId;
    private String bannerUid;
    private String bannerImageUrls;
    private Long productId;
    private String productType;
}
