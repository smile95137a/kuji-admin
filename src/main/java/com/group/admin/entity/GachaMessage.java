package com.group.admin.entity;

import lombok.Data;

@Data
public class GachaMessage {
    private Long id;
    private java.time.LocalDateTime createdDate;
    private String name;
    private String nickName;
    private String productDetail;
}
