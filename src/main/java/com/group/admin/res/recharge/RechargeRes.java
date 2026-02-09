package com.group.admin.res.recharge;

import com.group.admin.entity.RechargeRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 儲值記錄回應 DTO
 * 
 * @author Kuji Admin
 * @since 2026-02-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeRes {
    
    /** 儲值記錄 ID */
    private String id;
    
    /** 儲值方案 ID */
    private String planId;
    
    /** 儲值金額（台幣）*/
    private Long amount;
    
    /** 獲得金幣數量 */
    private Long goldCoins;
    
    /** 獲得紅利數量 */
    private Long bonusCoins;
    
    /** 支付方式 */
    private String paymentMethod;
    
    /** 支付狀態：PENDING（待支付）, COMPLETED（已完成）, FAILED（失敗）, REFUNDED（已退款） */
    private String paymentStatus;
    
    /** 支付閘道 */
    private String paymentGateway;
    
    /** 交易 ID（支付平台提供）*/
    private String transactionId;
    
    /** 失敗原因（如果支付失敗）*/
    private String failReason;
    
    /** 建立時間 */
    private LocalDateTime createdAt;
    
    /** 支付完成時間 */
    private LocalDateTime paidAt;
    
    /**
     * 從 Entity 轉換為 DTO
     */
    public static RechargeRes from(RechargeRecord record) {
        if (record == null) {
            return null;
        }
        
        return RechargeRes.builder()
                .id(record.getId())
                .planId(record.getPlanId())
                .amount(record.getAmount())
                .goldCoins(record.getGoldCoins())
                .bonusCoins(record.getBonusCoins())
                .paymentMethod(record.getPaymentMethod())
                .paymentStatus(record.getPaymentStatus())
                .paymentGateway(record.getPaymentGateway())
                .transactionId(record.getTransactionId())
                .failReason(record.getFailReason())
                .createdAt(record.getCreatedAt())
                .paidAt(record.getPaidAt())
                .build();
    }
}
