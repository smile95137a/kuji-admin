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
    
    private String lotteryId;
    private Boolean isLocked;
    private String lockedByUserId;
    private String lockedByUsername;
    private Boolean isLockedByCurrentUser;
    private Boolean canDraw;
    private Boolean isLockedByMe;
    private Boolean lockedByOther;
    private Integer protectionMinutes;
    private LocalDateTime lockStartTime;
    private LocalDateTime lockEndTime;
    private Long remainingLockSeconds;
    private String message;
}
