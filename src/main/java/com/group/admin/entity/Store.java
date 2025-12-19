package com.group.admin.entity;

import lombok.Data;

@Data
public class Store {
    private String id;
    private String ownerId;
    private String storeName;
    private String shortDescription;
    private String longDescription;
    private String logoUrl;
    private String coverImageUrl;
    private String email;
    private String phone;
    private String address;
    private String facebookUrl;
    private String instagramUrl;
    private String lineId;
    private String businessHours;
    private String status;
    private String remark;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private String updatedBy;
}
