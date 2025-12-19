package com.group.admin.entity;

import lombok.Data;

@Data
public class SignIn {
    private Long id;
    private java.time.LocalDateTime createdDate;
    private String number;
    private Double probability;
    private java.math.BigDecimal sliverPrice;
    private java.time.LocalDateTime updateDate;
}
