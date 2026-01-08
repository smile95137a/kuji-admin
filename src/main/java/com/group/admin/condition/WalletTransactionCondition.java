package com.group.admin.condition;

import com.group.admin.req.common.BaseCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 交易記錄查詢條件
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WalletTransactionCondition extends BaseCondition {
    
    /**
     * 玩家 ID
     */
    private String userId;
    
    /**
     * 交易類型
     */
    private String transactionType;
    
    /**
     * 幣種
     */
    private String coinType;
    
    /**
     * 關聯 ID
     */
    private String relatedId;
}
