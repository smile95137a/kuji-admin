package com.group.admin.service;

/**
 * 稽核日誌服務介面（032 規格重寫版）。
 * 所有方法均為非同步寫入（@Async），不阻塞請求流程。
 */
public interface AuditLogService {

    /**
     * 認證日誌：登入/登出/OAuth/Token 刷新。
     *
     * @param userId       使用者 ID（登入失敗時可能為 null）
     * @param userType     USER / ADMIN
     * @param email        登入 email
     * @param loginMethod  EMAIL / GOOGLE / REFRESH_TOKEN
     * @param result       SUCCESS / FAIL
     * @param errorMessage 失敗原因（成功時為 null）
     * @param ip           來源 IP
     * @param userAgent    瀏覽器資訊
     */
    void logAuth(String userId, String userType, String email,
                 String loginMethod, String result, String errorMessage,
                 String ip, String userAgent);

    /**
     * 後台管理操作日誌：CRUD、上下架、帳號管理等。
     *
     * @param adminId        後台操作者 ID
     * @param adminEmail     後台操作者 email
     * @param adminRole      操作時的角色
     * @param targetType     LOTTERY / STORE / ADMIN_USER / ORDER / PRIZE_BOX
     * @param targetId       被操作對象 ID（可選）
     * @param targetName     被操作對象名稱（snapshot，可選）
     * @param action         CREATE / UPDATE / DELETE / ON_SHELF / OFF_SHELF / ENABLE / DISABLE / RESET_PASSWORD
     * @param beforeSnapshot 操作前 JSON 快照（可選）
     * @param afterSnapshot  操作後 JSON 快照（可選）
     * @param result         SUCCESS / FAIL
     * @param errorMessage   失敗原因（成功時為 null）
     * @param ip             來源 IP
     */
    void logAdminAction(String adminId, String adminEmail, String adminRole,
                        String targetType, String targetId, String targetName,
                        String action, String beforeSnapshot, String afterSnapshot,
                        String result, String errorMessage, String ip);

    // --- 第二批實作（後續 spec 補充）---

    /** 抽獎日誌（第二批實作） */
    void logDraw(String userId, String lotteryId, String lotteryTitle,
                 String category, String playMode, String gameMode,
                 String ticketId, Integer ticketNumber, String prizeLevel,
                 String prizeName, Boolean isGrandPrize,
                 Long deductedGold, Long deductedBonus,
                 String result, String errorMessage, Integer durationMs);

    /** 儲值日誌（第二批實作） */
    void logRecharge(String userId, String rechargeId, String planId,
                     String planName, Long amount, Long goldAdded, Long bonusAdded,
                     String paymentMethod, String paymentGatewayRef,
                     String result, String errorMessage, String ip);

    /** 訂單操作日誌（第二批實作） */
    void logOrder(String operatorId, String operatorType, String orderId,
                  String action, Integer prizeBoxCount, Long totalAmount,
                  String trackingNumber, String result, String errorMessage);
}

