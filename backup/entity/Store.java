package com.group.admin.entity;

import lombok.Data;

@Data
public class Store {
    private String id;
    private String name;
    private String ownerAdminId;
    private Integer status;
    private String metadata;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
