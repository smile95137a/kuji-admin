package com.group.admin.entity;

import lombok.Data;

@Data
public class CustomerOrder {
    private String vendorOrder;
    private String errorCode;
    private String errorMessage;
    private String orderNo;
}
