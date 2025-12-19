package com.group.admin.entity;

import lombok.Data;

@Data
public class ShippingMethod {
    private Long shippingMethodId;
    private java.time.LocalDateTime createDate;
    private String description;
    private String name;
    private String shippingCode;
    private java.math.BigDecimal shippingPrice;
    private java.math.BigDecimal size;
    private Integer status;
    private java.time.LocalDateTime updateDate;
    private java.math.BigDecimal maxSize;
    private java.math.BigDecimal minSize;
    private String code;
}
