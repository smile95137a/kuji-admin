package com.group.admin.entity;

import lombok.Data;

@Data
public class Role {
    private String id;
    private String name;
    private String description;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
