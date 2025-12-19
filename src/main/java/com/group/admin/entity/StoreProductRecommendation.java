package com.group.admin.entity;

import lombok.Data;

@Data
public class StoreProductRecommendation {
    private Long id;
    private java.time.LocalDateTime createdDate;
    private String createdUser;
    private String recommendationName;
    private String updateUser;
    private java.time.LocalDateTime updatedDate;
    private String productDetailId;
}
