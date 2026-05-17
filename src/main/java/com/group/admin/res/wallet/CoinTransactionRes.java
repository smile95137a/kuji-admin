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
public class CoinTransactionRes {

    private String id;
    private String userId;
    private String userNickname;

    private String transactionType;
    private String transactionTypeName;

    // 舊前台相容欄位
    private String type;
    private String typeName;

    private String coinType;
    private String coinTypeName;
    private String direction;

    private Long amount;
    private Long goldAmount;
    private Long bonusAmount;
    private Long balanceAfter;

    private String relatedId;
    // 舊前台相容欄位
    private String referenceId;

    private String lotteryId;
    private String lotteryTitle;
    private Integer drawIndex;
    private Integer ticketNumber;
    private Long refundAmount;
    private String description;
    private String createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}