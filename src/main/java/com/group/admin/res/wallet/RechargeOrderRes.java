package com.group.admin.res.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeOrderRes {
    private String rechargeOrderId;
    private String payUrl;
    private String submitMethod;
    private String actionUrl;
    private Map<String, String> formFields;
    private Long goldAmount;
    private Long bonusAmount;
    private BigDecimal priceTwd;
    private String status;
    private LocalDateTime expiredAt;
}
