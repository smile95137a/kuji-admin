package com.group.admin.service;

import com.group.admin.entity.LotteryLock;

/**
 * 抽獎鎖定服務介面（保護時間機制）
 * 所有 ID 都使用 String (UUID)
 *
 * @author KUJI System
 * @since 1.0.0
 */
public interface LotteryLockService {

    /**
     * 檢查並取得鎖定狀態
     *
     * @param lotteryId 商品ID (UUID)
     * @param userId    當前使用者ID (UUID)
     * @return 鎖定狀態資訊
     */
    LockStatus checkLockStatus(String lotteryId, String userId);

    /**
     * 嘗試取得鎖定（首次抽獎時調用）
     *
     * @param lotteryId 商品ID (UUID)
     * @param userId    使用者ID (UUID)
     * @return 是否成功取得鎖定
     */
    boolean tryAcquireLock(String lotteryId, String userId);

    /**
     * 釋放鎖定（抽獎結束或超時時調用）
     *
     * @param lotteryId 商品ID (UUID)
     * @param userId    使用者ID (UUID)
     */
    void releaseLock(String lotteryId, String userId);

    /**
     * 清理過期鎖定（定時任務調用）
     */
    void cleanExpiredLocks();

    /**
     * 查詢商品的當前鎖定（如果有）
     *
     * @param lotteryId 商品ID (UUID)
     * @return 鎖定資訊，如果沒有則為 null
     */
    LotteryLock getActiveLock(String lotteryId);

    /**
     * 鎖定狀態資訊
     */
    record LockStatus(
            boolean canDraw,           // 是否可以抽獎
            boolean isLockedByMe,      // 是否被自己鎖定
            String lockedByUserId,     // 被誰鎖定 (UUID)
            Long remainingSeconds      // 鎖定剩餘秒數
    ) {}
}
