package com.group.admin.res.draw;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 鎖定狀態回應 DTO
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Data
public class LockStatusRes {
    
    /**
     * 商品ID
     */
    private Long lotteryId;
    
    /**
     * 是否被鎖定
     */
    private Boolean isLocked;
    
    /**
     * 鎖定者用戶ID（如果被鎖定）
     */
    private Long lockedByUserId;
    
    /**
     * 鎖定者用戶名稱
     */
    private String lockedByUsername;
    
    /**
     * 是否為當前用戶鎖定
     */
    private Boolean isLockedByCurrentUser;
    
    /**
     * 鎖定開始時間
     */
    private LocalDateTime lockStartTime;
    
    /**
     * 鎖定結束時間
     */
    private LocalDateTime lockEndTime;
    
    /**
     * 剩餘鎖定時間（秒）
     */
    private Long remainingLockSeconds;
    
    /**
     * 提示訊息
     */
    private String message;
}
