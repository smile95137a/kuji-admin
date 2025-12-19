package com.group.admin.entity;

import lombok.Data;

@Data
public class RankingList {
    private Long id;
    private String category;
    private java.time.LocalDateTime createdDate;
    private String name;
    private String nickname;
    private Integer productCount;
    private String productId;
    private String status;
}
