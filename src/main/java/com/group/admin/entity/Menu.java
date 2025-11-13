package com.group.admin.entity;

import lombok.Data;

@Data
public class Menu {
    private String id;
    private String name;
    private String path;
    private String parentId;
    private String icon;
    private Integer orderNum;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
