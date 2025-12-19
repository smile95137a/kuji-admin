package com.group.admin.entity;

import lombok.Data;

@Data
public class AdminUser {
    private String id;
    private String username;
    private String password;
    private Integer status;
    private java.time.LocalDateTime lastLogin;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
