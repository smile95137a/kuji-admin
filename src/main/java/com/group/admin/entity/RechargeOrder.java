package com.group.admin.entity;

import com.group.admin.enums.RechargeOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeOrder {
    private String id;
    private String userId;
    private String planId;
    private Long goldAmount;
    private Long bonusAmount;
    private BigDecimal priceTwd;
    private RechargeOrderStatus status;
    private String paymentMethod;
    private String gatewayProvider;
    private String gatewayOrderId;
    private String gatewayResult;
    private String gatewayRetMsg;
    private String virtualAccount;
    private String paymentInfo;
    private String limitDate;
    private String gatewayRawResp;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private LocalDateTime paidAt;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
