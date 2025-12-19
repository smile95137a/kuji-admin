package com.group.admin.entity;

import lombok.Data;

@Data
public class StoreUser {
    private String id;
    private String storeId;
    private String adminUserId;
    private String roleType;
    private java.time.LocalDateTime createdAt;
}
