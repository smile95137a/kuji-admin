package com.group.admin.entity;

import lombok.Data;

@Data
public class Menu {
    private String id;
    private String name;
    private String code;
    private String path;
    private String parentId;
    private String icon;
    private Integer orderNum;
    private Boolean isVisible;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
