package com.group.admin.service.logistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingResult {
    private boolean success;
    private String trackingNumber;
    private String message;
}
