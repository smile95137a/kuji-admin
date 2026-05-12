package com.group.admin.service;

/**
 * 郵件服務接口
 */
public interface EmailService {
    
    /**
     * 發送驗證碼郵件（會員註冊）
     */
    void sendVerificationEmail(String email, String nickname, String verificationCode);
    
    /**
     * 發送密碼重設郵件
     */
    void sendPasswordResetEmail(String email, String nickname, String resetToken);
    
    /**
     * 發送訂單通知郵件
     */
    void sendOrderNotification(String email, String nickname, String orderNumber, String orderStatus);
    
    /**
     * 發送通用通知郵件
     */
    void sendNotification(String email, String nickname, String subject, String content);
    
    /**
     * 發送初始密碼郵件（後台帳號建立時）
     */
    void sendInitialPasswordEmail(String to, String displayName, String initialPassword);

    /**
     * 發送初始密碼郵件（同步模式，失敗時拋出例外）
     */
    void sendInitialPasswordEmailSync(String to, String displayName, String initialPassword);

    /**
     * 發送臨時密碼郵件（忘記密碼）
     */
    void sendTemporaryPasswordEmail(String to, String displayName, String temporaryPassword,
                                    String loginUrl, String scene);

    /**
     * 發送臨時密碼郵件（同步模式，失敗時拋出例外）
     */
    void sendTemporaryPasswordEmailSync(String to, String displayName, String temporaryPassword,
                                        String loginUrl, String scene);
    
    /**
     * 重試發送失敗的郵件
     */
    void retryFailedEmails();
}
