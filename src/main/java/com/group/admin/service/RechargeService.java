package com.group.admin.service;

import com.group.admin.req.recharge.RechargeReq;
import com.group.admin.res.recharge.RechargeRes;
import com.group.admin.gateway.GatewayCallbackResult;
import com.group.admin.res.wallet.RechargeOrderRes;

/**
 * 前台使用者儲值服務介面
 * 
 * @author Kuji Admin
 * @since 2026-02-08
 */
public interface RechargeService {
    
    /**
     * 建立儲值請求
     * 
     * 流程：
     * 1. 查詢儲值方案（驗證 planId 存在且有效）
     * 2. 建立 RechargeRecord（狀態 = PENDING）
     * 3. 暫時不直接加金幣（等支付成功後再加）
     * 4. 返回儲值記錄，等前端完成支付後調用確認 API
     * 
     * @param userId 使用者 ID
     * @param req 儲值請求
     * @return 儲值記錄
     */
    RechargeRes createRechargeRequest(String userId, RechargeReq req);
    
    /**
     * 查詢使用者的儲值記錄（分頁）
     * 
     * @param userId 使用者 ID
     * @param page 分頁頁碼（從 1 開始）
     * @param size 每頁筆數
     * @return 儲值記錄列表
     */
    java.util.List<RechargeRes> getUserRechargeHistory(String userId, Integer page, Integer size);
    
    /**
     * 確認支付（模擬支付網關回調）
     * 
     * 流程：
     * 1. 查詢 RechargeRecord
     * 2. 驗證狀態為 PENDING
     * 3. 更新狀態為 COMPLETED，記錄支付時間
     * 4. 更新 User 的 goldCoins + bonusCoins
     * 5. 建立 WalletTransaction 記錄（類型 = RECHARGE）
     * 
     * @param rechargeId 儲值記錄 ID
     * @param transactionId 支付平台的交易 ID（選填）
     * @return 儲值記錄
     */
    RechargeRes confirmPayment(String rechargeId, String transactionId);
    
    /**
     * 記錄支付失敗
     * 
     * @param rechargeId 儲值記錄 ID
     * @param failReason 失敗原因
     * @return 儲值記錄
     */
    RechargeRes recordPaymentFailure(String rechargeId, String failReason);

    /**
     * 建立儲值訂單（透過支付閘道）
     *
     * @param userId 玩家 ID
     * @param planId 方案 ID
     * @return 訂單資訊（含支付 URL）
     */
    RechargeOrderRes createRechargeOrder(String userId, String planId);

    /**
     * 處理支付閘道回調
     *
     * @param result 閘道回調結果
     */
    void handleCallback(GatewayCallbackResult result);
}
