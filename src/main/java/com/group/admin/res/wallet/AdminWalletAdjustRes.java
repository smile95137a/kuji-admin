package com.group.admin.res.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWalletAdjustRes {
    private String userId;
    private String currency;
    private Long delta;
    private Long goldBalanceAfter;
    private Long bonusBalanceAfter;
    private String transactionId;
    private String adminId;
    private String reason;
    private LocalDateTime adjustedAt;
}
