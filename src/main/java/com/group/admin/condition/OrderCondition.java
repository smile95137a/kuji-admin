package com.group.admin.condition;

import com.group.admin.req.common.BaseCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCondition extends BaseCondition {

    private String orderNo;
    private String storeId;
    private String userId;
    private String shippingMethod;
    private String status;
    private String shippingStatus;
    private String recipientName;
    private String recipientPhone;
}