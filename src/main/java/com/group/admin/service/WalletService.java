package com.group.admin.service;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.wallet.WalletAdjustReq;
import com.group.admin.res.wallet.UserWalletRes;
import com.group.admin.res.wallet.WalletTransactionRes;
import com.group.admin.condition.WalletTransactionCondition;

import java.util.List;

/**
 * 錢包服務介面
 * 
 * @author Kuji Admin
 * @since 2026-01-09
 */
public interface WalletService {
    
    /**
     * 建立錢包（註冊時自動建立）
     * 
     * @param userId 玩家 ID
     * @return 錢包資訊
     */
    UserWalletRes createWallet(String userId);
    
    /**
     * 查詢錢包資訊
     * 
     * @param userId 玩家 ID
     * @return 錢包資訊
     */
    UserWalletRes getWallet(String userId);
    
    /**
     * 扣除金幣（抽獎消費）
     * 使用樂觀鎖確保併發安全
     * 
     * @param userId 玩家 ID
     * @param amount 扣除金額
     * @param transactionType 交易類型
     * @param relatedId 關聯 ID（抽獎ID等）
     * @param description 說明
     */
    void deductGold(String userId, Long amount, String transactionType, String relatedId, String description);
    
    /**
     * 增加金幣（儲值）
     * 
     * @param userId 玩家 ID
     * @param amount 增加金額
     * @param transactionType 交易類型
     * @param relatedId 關聯 ID（儲值ID等）
     * @param description 說明
     */
    void addGold(String userId, Long amount, String transactionType, String relatedId, String description);
    
    /**
     * 增加紅利（回收獎品、活動贈送）
     * 
     * @param userId 玩家 ID
     * @param amount 增加金額
     * @param transactionType 交易類型
     * @param relatedId 關聯 ID
     * @param description 說明
     */
    void addBonus(String userId, Long amount, String transactionType, String relatedId, String description);
    
    /**
     * 手動調整點數（Admin）
     * 
     * @param req 調整請求
     * @param operatorId 操作者 ID
     */
    void adjustCoins(WalletAdjustReq req, String operatorId);
    
    /**
     * 查詢交易記錄
     * 
     * @param req 查詢請求
     * @return 交易記錄列表
     */
    List<WalletTransactionRes> getTransactions(QueryReq<WalletTransactionCondition> req);
    
    /**
     * 檢查餘額是否足夠
     * 
     * @param userId 玩家 ID
     * @param amount 需要的金額
     * @return 是否足夠
     */
    boolean hasEnoughGold(String userId, Long amount);
}
