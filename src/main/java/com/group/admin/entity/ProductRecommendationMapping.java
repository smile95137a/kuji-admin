package com.group.admin.entity;

import lombok.Data;

@Data
public class ProductRecommendationMapping {
    private Long id;
    private java.time.LocalDateTime createdDate;
    private String createdUser;
    private Long storeProductId;
    private Long storeProductRecommendationId;
    private String updateUser;
    private java.time.LocalDateTime updatedDate;
    private String productName;
    private String recommendationName;
    private String productDetailId;
    private String imageUrls;
}
