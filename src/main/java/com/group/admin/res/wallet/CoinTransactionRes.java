package com.group.admin.res.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 金幣交易記錄回應
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinTransactionRes {
    
    /**
     * 交易 ID
     */
    private String id;
    
    /**
     * 玩家 ID
     */
    private String userId;
    
    /**
     * 玩家暱稱（冗餘）
     */
    private String userNickname;
    
    /**
     * 交易類型代碼
     */
    private String transactionType;
    
    /**
     * 交易類型名稱
     */
    private String transactionTypeName;
    
    /**
     * 幣種代碼
     */
    private String coinType;
    
    /**
     * 幣種名稱
     */
    private String coinTypeName;
    
    /**
     * 金額（正數=增加，負數=減少）
     */
    private Long amount;
    
    /**
     * 異動後餘額
     */
    private Long balanceAfter;
    
    /**
     * 關聯 ID（抽獎ID、訂單ID、儲值ID等）
     */
    private String relatedId;
    
    /**
     * 說明
     */
    private String description;
    
    /**
     * 操作者 ID（系統調整時記錄管理員）
     */
    private String createdBy;
    
    /**
     * 操作者名稱
     */
    private String createdByName;
    
    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
}
