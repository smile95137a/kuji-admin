package com.group.admin.service;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.consumption.ConsumptionRecordCondition;
import com.group.admin.res.consumption.ConsumptionRecordRes;

import java.util.List;

/**
 * 消費紀錄 Service 介面
 * 
 * 只記錄以下情境的消費：
 * 1. 使用金幣抽獎 (DRAW_GOLD)
 * 2. 使用紅利抽獎 (DRAW_BONUS)
 * 3. 訂單成立支付運費 (SHIPPING_FEE)
 * 
 * ⚠️ 儲值不是消費紀錄！儲值有自己的 recharge_record 表。
 */
public interface ConsumptionRecordService {
    
    /**
     * 記錄消費（內部呼叫）
     */
    void recordConsumption(String userId, String type, String lotteryId, String lotteryTitle,
                           String orderId, String orderNumber, Long goldAmount, Long bonusAmount, String description);
    
    /**
     * 查詢消費紀錄（前台 - 查自己的）
     */
    List<ConsumptionRecordRes> getMyConsumptions(String userId, QueryReq<ConsumptionRecordCondition> req);
    
    /**
     * 查詢消費紀錄（後台 - 查所有人的）
     */
    List<ConsumptionRecordRes> queryConsumptions(QueryReq<ConsumptionRecordCondition> req);
}
