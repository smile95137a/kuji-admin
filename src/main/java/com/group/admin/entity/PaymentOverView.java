package com.group.admin.entity;

import lombok.Data;

@Data
public class PaymentOverView {
    private Long id;
    private java.time.LocalDateTime createDate;
    private java.time.LocalDateTime lastTransactionDate;
    private java.math.BigDecimal totalBonus;
    private java.math.BigDecimal totalGold;
    private java.math.BigDecimal totalSilver;
    private java.math.BigDecimal totalSpentBonus;
    private java.math.BigDecimal totalSpentGold;
    private java.math.BigDecimal totalSpentSilver;
    private java.time.LocalDateTime updateDate;
    private Long userId;
}
