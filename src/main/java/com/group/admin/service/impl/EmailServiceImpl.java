package com.group.admin.service.impl;

import com.group.admin.config.AppUrlProperties;
import com.group.admin.service.EmailDispatchService;
import com.group.admin.service.EmailService;
import com.group.admin.service.MailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 業務郵件服務實作。
 * 僅負責郵件場景組裝，不直接處理 SMTP 傳輸與寄送日誌。
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final MailTemplateService mailTemplateService;
    private final EmailDispatchService emailDispatchService;
    private final AppUrlProperties appUrlProperties;
    
    @Value("${app.name:KUJI 一番賞}")
    private String appName;
    
    @Override
    @Async
    public void sendVerificationEmail(String email, String nickname, String verificationCode) {
        String subject = String.format("[%s] 信箱驗證碼", appName);

        Map<String, Object> params = Map.of(
            "nickname", nickname,
            "verificationCode", verificationCode
        );

        String content = mailTemplateService.render("verification-email", params);
        dispatchEmail("VERIFICATION", email, nickname, subject, content, "verification-email", params, null, null);
    }
    
    @Override
    @Async
    public void sendPasswordResetEmail(String email, String nickname, String resetToken) {
        String subject = String.format("[%s] 密碼重設請求", appName);
        String resetUrl = appUrlProperties.getClientBaseUrl() + "/reset-password?token=" + resetToken;

        Map<String, Object> params = Map.of(
            "nickname", nickname,
            "resetUrl", resetUrl,
            "resetToken", resetToken
        );

        String content = mailTemplateService.render("password-reset-email", Map.of(
                "nickname", nickname,
                "resetUrl", resetUrl,
                "resetToken", resetToken,
                "expiryMinutes", "30"
        ));
        dispatchEmail("PASSWORD_RESET", email, nickname, subject, content, "password-reset-email", params, null, null);
    }
    
    @Override
    @Async
    public void sendOrderNotification(String email, String nickname, String orderNumber, String orderStatus) {
        String statusText = getOrderStatusText(orderStatus);
        String subject = String.format("[%s] 訂單%s通知 - %s", appName, statusText, orderNumber);
        String content = buildOrderNotificationContent(nickname, orderNumber, statusText);

        Map<String, Object> params = Map.of(
            "nickname", nickname,
            "orderNumber", orderNumber,
            "orderStatus", orderStatus,
            "statusText", statusText
        );

        dispatchEmail("ORDER", email, nickname, subject, content, "order_notification", params, "ORDER", orderNumber);
    }
    
    @Override
    @Async
    public void sendNotification(String email, String nickname, String subject, String content) {
        dispatchEmail("NOTIFICATION", email, nickname, subject, content, null, null, null, null);
    }
    
    @Override
    @Async
    public void sendInitialPasswordEmail(String to, String displayName, String initialPassword) {
        sendInitialPasswordEmailSync(to, displayName, initialPassword);
    }

    @Override
    public void sendInitialPasswordEmailSync(String to, String displayName, String initialPassword) {
        String subject = String.format("[%s] 後台帳號初始密碼", appName);

        Map<String, Object> params = Map.of(
            "displayName", displayName,
            "initialPassword", initialPassword
        );

        String content = mailTemplateService.render("initial-password-email", Map.of(
                "displayName", displayName,
                "initialPassword", initialPassword,
                "loginUrl", appUrlProperties.getAdminLoginUrl()
        ));

        dispatchEmailOrThrow("INITIAL_PASSWORD", to, displayName, subject, content,
                "initial-password-email", params, null, null);
    }

    @Override
    @Async
    public void sendTemporaryPasswordEmail(String to, String displayName, String temporaryPassword,
                                           String loginUrl, String scene) {
        sendTemporaryPasswordEmailSync(to, displayName, temporaryPassword, loginUrl, scene);
    }

    @Override
    public void sendTemporaryPasswordEmailSync(String to, String displayName, String temporaryPassword,
                                               String loginUrl, String scene) {
        String safeScene = scene != null && !scene.isBlank() ? scene : "忘記密碼";
        String safeDisplayName = displayName != null && !displayName.isBlank() ? displayName : "使用者";
        String safeLoginUrl = loginUrl != null && !loginUrl.isBlank() ? loginUrl : appUrlProperties.getClientLoginUrl();
        String subject = String.format("[%s] 臨時密碼通知", appName);

        Map<String, Object> params = Map.of(
            "displayName", safeDisplayName,
            "temporaryPassword", temporaryPassword,
            "loginUrl", safeLoginUrl,
            "scene", safeScene
        );

        String content = mailTemplateService.render("temporary-password-email", params);

        dispatchEmailOrThrow("TEMP_PASSWORD", to, safeDisplayName, subject, content,
                "temporary-password-email", params, null, null);
    }

    @Override
    public void retryFailedEmails() {
        emailDispatchService.retryFailedEmails();
    }

    private void dispatchEmail(String emailType, String toEmail, String toName, String subject,
                               String content, String templateName, Map<String, Object> templateParams,
                               String relatedType, String relatedId) {
        emailDispatchService.send(emailType, toEmail, toName, subject, content,
                templateName, templateParams, relatedType, relatedId, false);
    }

    private void dispatchEmailOrThrow(String emailType, String toEmail, String toName, String subject,
                                      String content, String templateName, Map<String, Object> templateParams,
                                      String relatedType, String relatedId) {
        emailDispatchService.send(emailType, toEmail, toName, subject, content,
                templateName, templateParams, relatedType, relatedId, true);
    }

    private String buildOrderNotificationContent(String nickname, String orderNumber, String statusText) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .order-info { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>訂單狀態更新</h1>
                    </div>
                    <div class="content">
                        <p>親愛的 <strong>%s</strong>，您好！</p>
                        <p>您的訂單狀態已更新：</p>
                        <div class="order-info">
                            <p><strong>訂單編號：</strong>%s</p>
                            <p><strong>最新狀態：</strong>%s</p>
                        </div>
                        <p>您可以登入會員中心查看詳細訂單資訊。</p>
                    </div>
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆。</p>
                        <p>&copy; %d %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, nickname, orderNumber, statusText,
            LocalDateTime.now().getYear(), appName);
    }
    
    private String getOrderStatusText(String status) {
        return switch (status) {
            case "PENDING" -> "待處理";
            case "PREPARING" -> "準備出貨";
            case "SHIPPED" -> "已出貨";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }
}
