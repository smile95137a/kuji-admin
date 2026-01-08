package com.group.admin.condition;

import com.group.admin.req.common.BaseCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 訂單查詢條件
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCondition extends BaseCondition {
    
    /**
     * 訂單編號（模糊查詢）
     */
    private String orderNo;
    
    /**
     * 店家 ID（後台自動帶入，前台不使用）
     */
    private String storeId;
    
    /**
     * 玩家 ID
     */
    private String userId;
    
    /**
     * 配送方式
     */
    private String shippingMethod;
    
    /**
     * 訂單狀態
     */
    private String shippingStatus;
    
    /**
     * 收件人姓名（模糊查詢）
     */
    private String recipientName;
    
    /**
     * 收件人電話（模糊查詢）
     */
    private String recipientPhone;
}
