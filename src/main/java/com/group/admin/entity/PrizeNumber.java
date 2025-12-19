package com.group.admin.entity;

import lombok.Data;

@Data
public class PrizeNumber {
    private Long prizeNumberId;
    private Boolean isDrawn;
    private String level;
    private String number;
    private Long productDetailId;
    private Long productId;
    private Double probability;
}
