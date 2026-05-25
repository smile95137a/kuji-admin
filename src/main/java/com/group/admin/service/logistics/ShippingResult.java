package com.group.admin.service.logistics;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingResult {
    private boolean success;
    private String trackingNumber;
    private String message;
    private String providerOrderNo;
    private String provider;
    private String trackingUrl;
    private String rawPayload;
}
