package com.group.admin.entity;

import lombok.Data;

@Data
public class Role {
    private String id;
    private String name;
    private String code;
    private String description;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
