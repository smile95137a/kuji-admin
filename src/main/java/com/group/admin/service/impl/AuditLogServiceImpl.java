package com.group.admin.service.impl;

import com.group.admin.entity.LogAdminAction;
import com.group.admin.entity.LogAuth;
import com.group.admin.entity.LogDraw;
import com.group.admin.entity.LogOrder;
import com.group.admin.entity.LogRecharge;
import com.group.admin.mapper.LogAdminActionMapper;
import com.group.admin.mapper.LogAuthMapper;
import com.group.admin.mapper.LogDrawMapper;
import com.group.admin.mapper.LogOrderMapper;
import com.group.admin.mapper.LogRechargeMapper;
import com.group.admin.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 500;

    private final LogAuthMapper        logAuthMapper;
    private final LogAdminActionMapper logAdminActionMapper;
    private final LogDrawMapper        logDrawMapper;
    private final LogRechargeMapper    logRechargeMapper;
    private final LogOrderMapper       logOrderMapper;

    // ---------------------------------------------------------------
    // 認證日誌
    // ---------------------------------------------------------------

    @Async("taskExecutor")
    @Override
    public void logAuth(String userId, String userType, String email,
                        String loginMethod, String result, String errorMessage,
                        String ip, String userAgent) {
        try {
            LogAuth record = new LogAuth();
            record.setId(UUID.randomUUID().toString());
            record.setUserId(userId);
            record.setUserType(userType);
            record.setEmail(email);
            record.setLoginMethod(loginMethod);
            record.setResult(result);
            record.setErrorMessage(limitLength(errorMessage, ERROR_MESSAGE_MAX_LENGTH));
            record.setIp(ip);
            record.setUserAgent(userAgent);
            record.setCreatedAt(LocalDateTime.now());
            logAuthMapper.insertSelective(record);
        } catch (Exception e) {
            log.warn("⚠️ [AuditLog] 認證日誌寫入失敗: {}", e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 後台管理操作日誌
    // ---------------------------------------------------------------

    @Async("taskExecutor")
    @Override
    public void logAdminAction(String adminId, String adminEmail, String adminRole,
                               String targetType, String targetId, String targetName,
                               String action, String beforeSnapshot, String afterSnapshot,
                               String result, String errorMessage, String ip) {
        try {
            LogAdminAction record = new LogAdminAction();
            record.setId(UUID.randomUUID().toString());
            record.setAdminId(adminId);
            record.setAdminEmail(adminEmail);
            record.setAdminRole(adminRole);
            record.setTargetType(targetType);
            record.setTargetId(targetId);
            record.setTargetName(targetName);
            record.setAction(action);
            record.setBeforeSnapshot(beforeSnapshot);
            record.setAfterSnapshot(afterSnapshot);
            record.setResult(result);
            record.setErrorMessage(limitLength(errorMessage, ERROR_MESSAGE_MAX_LENGTH));
            record.setIp(ip);
            record.setCreatedAt(LocalDateTime.now());
            logAdminActionMapper.insertSelective(record);
        } catch (Exception e) {
            log.warn("⚠️ [AuditLog] 後台操作日誌寫入失敗: {}", e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 抽獎日誌（第二批實作）
    // ---------------------------------------------------------------

    @Async("taskExecutor")
    @Override
    public void logDraw(String userId, String lotteryId, String lotteryTitle,
                        String category, String playMode, String gameMode,
                        String ticketId, Integer ticketNumber, String prizeLevel,
                        String prizeName, Boolean isGrandPrize,
                        Long deductedGold, Long deductedBonus,
                        String result, String errorMessage, Integer durationMs) {
        try {
            LogDraw record = new LogDraw();
            record.setId(UUID.randomUUID().toString());
            record.setUserId(userId);
            record.setLotteryId(lotteryId);
            record.setLotteryTitle(lotteryTitle);
            record.setCategory(category);
            record.setPlayMode(playMode);
            record.setGameMode(gameMode);
            record.setTicketId(ticketId);
            record.setTicketNumber(ticketNumber);
            record.setPrizeLevel(prizeLevel);
            record.setPrizeName(prizeName);
            record.setIsGrandPrize(Boolean.TRUE.equals(isGrandPrize) ? (byte) 1 : (byte) 0);
            record.setDeductedGold(deductedGold);
            record.setDeductedBonus(deductedBonus);
            record.setResult(result);
            record.setErrorMessage(limitLength(errorMessage, ERROR_MESSAGE_MAX_LENGTH));
            record.setDurationMs(durationMs);
            record.setCreatedAt(LocalDateTime.now());
            logDrawMapper.insertSelective(record);
        } catch (Exception e) {
            log.warn("⚠️ [AuditLog] 抽獎日誌寫入失敗: {}", e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 儲值日誌（第二批實作）
    // ---------------------------------------------------------------

    @Async("taskExecutor")
    @Override
    public void logRecharge(String userId, String rechargeId, String planId,
                            String planName, Long amount, Long goldAdded, Long bonusAdded,
                            String paymentMethod, String paymentGatewayRef,
                            String result, String errorMessage, String ip) {
        try {
            LogRecharge record = new LogRecharge();
            record.setId(UUID.randomUUID().toString());
            record.setUserId(userId);
            record.setRechargeId(rechargeId);
            record.setPlanId(planId);
            record.setPlanName(planName);
            record.setAmount(amount);
            record.setGoldAdded(goldAdded);
            record.setBonusAdded(bonusAdded);
            record.setPaymentMethod(paymentMethod);
            record.setPaymentGatewayRef(paymentGatewayRef);
            record.setResult(result);
            record.setErrorMessage(limitLength(errorMessage, ERROR_MESSAGE_MAX_LENGTH));
            record.setIp(ip);
            record.setCreatedAt(LocalDateTime.now());
            logRechargeMapper.insertSelective(record);
        } catch (Exception e) {
            log.warn("⚠️ [AuditLog] 儲值日誌寫入失敗: {}", e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // 訂單操作日誌（第二批實作）
    // ---------------------------------------------------------------

    @Async("taskExecutor")
    @Override
    public void logOrder(String operatorId, String operatorType, String orderId,
                         String action, Integer prizeBoxCount, Long totalAmount,
                         String trackingNumber, String result, String errorMessage) {
        try {
            LogOrder record = new LogOrder();
            record.setId(UUID.randomUUID().toString());
            record.setOperatorId(operatorId);
            record.setOperatorType(operatorType);
            record.setOrderId(orderId);
            record.setAction(action);
            record.setPrizeBoxCount(prizeBoxCount);
            record.setTotalAmount(totalAmount);
            record.setTrackingNumber(trackingNumber);
            record.setResult(result);
            record.setErrorMessage(limitLength(errorMessage, ERROR_MESSAGE_MAX_LENGTH));
            record.setCreatedAt(LocalDateTime.now());
            logOrderMapper.insertSelective(record);
        } catch (Exception e) {
            log.warn("⚠️ [AuditLog] 訂單操作日誌寫入失敗: {}", e.getMessage());
        }
    }

    private String limitLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

