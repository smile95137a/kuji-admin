package com.group.admin.entity;

import lombok.Data;

@Data
public class VendorOrder {
    private String vendorOrder;
    private String errorCode;
    private String errorMessage;
    private String orderNo;
    private String express;
    private String status;
}
