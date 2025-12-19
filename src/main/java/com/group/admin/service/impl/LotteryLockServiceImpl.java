package com.group.admin.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group.admin.entity.LotteryLock;
import com.group.admin.example.LotteryLockExample;
import com.group.admin.mapper.LotteryLockMapper;
import com.group.admin.service.LotteryLockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽獎鎖定服務實作（保護時間機制）
 * 使用 Example 模式查詢
 * 所有 ID 都是 UUID String
 *
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryLockServiceImpl implements LotteryLockService {

    private final LotteryLockMapper lotteryLockMapper;

    /**
     * 預設保護時間（分鐘）
     */
    private static final int DEFAULT_LOCK_MINUTES = 5;

    @Override
    public LockStatus checkLockStatus(String lotteryId, String userId) {
        LotteryLock lock = getActiveLock(lotteryId);
        
        if (lock == null) {
            // 沒有鎖定，可以抽獎
            return new LockStatus(true, false, null, 0L);
        }
        
        // 檢查鎖定是否過期
        if (lock.getLockEndTime().isBefore(LocalDateTime.now())) {
            // 鎖定已過期，釋放並允許抽獎
            releaseLock(lotteryId, lock.getUserId());
            return new LockStatus(true, false, null, 0L);
        }
        
        // 計算剩餘秒數
        long remainingSeconds = Duration.between(LocalDateTime.now(), lock.getLockEndTime()).getSeconds();
        
        if (lock.getUserId().equals(userId)) {
            // 被自己鎖定，可以繼續抽獎
            return new LockStatus(true, true, userId, remainingSeconds);
        }
        
        // 被他人鎖定，不可抽獎
        return new LockStatus(false, false, lock.getUserId(), remainingSeconds);
    }

    @Override
    @Transactional
    public boolean tryAcquireLock(String lotteryId, String userId) {
        log.info("嘗試取得鎖定: lotteryId={}, userId={}", lotteryId, userId);
        
        // 先檢查現有鎖定
        LotteryLock existingLock = getActiveLock(lotteryId);
        
        if (existingLock != null) {
            // 檢查是否過期
            if (existingLock.getLockEndTime().isBefore(LocalDateTime.now())) {
                // 過期，釋放並繼續
                releaseLock(lotteryId, existingLock.getUserId());
            } else if (existingLock.getUserId().equals(userId)) {
                // 已經是自己的鎖定，可以繼續
                log.info("已存在自己的鎖定: lotteryId={}, userId={}", lotteryId, userId);
                return true;
            } else {
                // 被他人鎖定
                log.warn("商品已被他人鎖定: lotteryId={}, lockedBy={}", lotteryId, existingLock.getUserId());
                return false;
            }
        }
        
        // 建立新鎖定
        LotteryLock lock = new LotteryLock();
        lock.setId(UUID.randomUUID().toString());
        lock.setLotteryId(lotteryId);
        lock.setUserId(userId);
        lock.setLockStartTime(LocalDateTime.now());
        lock.setLockEndTime(LocalDateTime.now().plusMinutes(DEFAULT_LOCK_MINUTES));
        lock.setIsActive((Byte.valueOf("1")));
        lock.setCreatedAt(LocalDateTime.now());
        
        lotteryLockMapper.insert(lock);
        log.info("鎖定建立成功: lotteryId={}, userId={}, 到期時間={}", lotteryId, userId, lock.getLockEndTime());
        
        return true;
    }

    @Override
    @Transactional
    public void releaseLock(String lotteryId, String userId) {
        log.info("釋放鎖定: lotteryId={}, userId={}", lotteryId, userId);
        
        // 使用 Example 模式查詢並更新
        LotteryLockExample example = new LotteryLockExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andUserIdEqualTo(userId)
                .andIsActiveEqualTo((Byte.valueOf("1")));
        
        List<LotteryLock> locks = lotteryLockMapper.selectByExample(example);
        for (LotteryLock lock : locks) {
            lock.setIsActive((Byte.valueOf("0")));
            lotteryLockMapper.updateByPrimaryKey(lock);
        }
    }

    @Override
    @Transactional
    public void cleanExpiredLocks() {
        log.info("開始清理過期鎖定");
        
        // 查詢所有活躍但已過期的鎖定
        LotteryLockExample example = new LotteryLockExample();
        example.createCriteria().andIsActiveEqualTo((Byte.valueOf("1")));
        
        List<LotteryLock> locks = lotteryLockMapper.selectByExample(example);
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        
        for (LotteryLock lock : locks) {
            if (lock.getLockEndTime().isBefore(now)) {
                lock.setIsActive((Byte.valueOf("0")));
                lotteryLockMapper.updateByPrimaryKey(lock);
                count++;
            }
        }
        
        log.info("清理過期鎖定完成: count={}", count);
    }

    @Override
    public LotteryLock getActiveLock(String lotteryId) {
        LotteryLockExample example = new LotteryLockExample();
        example.createCriteria()
                .andLotteryIdEqualTo(lotteryId)
                .andIsActiveEqualTo((Byte.valueOf("1")));
        
        List<LotteryLock> locks = lotteryLockMapper.selectByExample(example);
        return locks.isEmpty() ? null : locks.get(0);
    }
}
