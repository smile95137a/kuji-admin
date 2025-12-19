package com.group.admin.entity;

import lombok.Data;

@Data
public class User {
    private String id;
    private String email;
    private String nickname;
    private String password;
    private String avatar;
    private Long goldCoins;
    private Long bonusCoins;
    private Integer status;
    private java.time.LocalDateTime lastLogin;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
