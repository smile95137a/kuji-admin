package com.group.admin.service.logistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingInfo {
    private String shippingMethodCode;
    private Long amount;
    private String orderNumber;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String storeCode;
    private String storeName;
    private String storeType;
}
