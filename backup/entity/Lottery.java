package com.group.admin.entity;

import lombok.Data;

@Data
public class Lottery {
    private String id;
    private String storeId;
    private String title;
    private String description;
    private Long pricePerDraw;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
    private Integer status;
    private Integer totalDraws;
    private Integer maxDraws;
    private String createBy;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime updateDate;
}
