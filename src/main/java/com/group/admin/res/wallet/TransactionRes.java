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
public class TransactionRes {
    private String id;
    private String transactionType;
    private Long goldDelta;
    private Long bonusDelta;
    private Long goldAfter;
    private Long bonusAfter;
    private String referenceId;
    private String reason;
    private LocalDateTime createdAt;
}
