package com.group.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.entity.EmailLog;
import com.group.admin.mapper.EmailLogMapper;
import com.group.admin.repository.EmailLogRepository;
import com.group.admin.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 郵件服務實作（使用 Gmail SMTP + Thymeleaf 模板）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailLogMapper emailLogMapper;
    private final EmailLogRepository emailLogRepository;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${app.name:KUJI 一番賞}")
    private String appName;
    
    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;
    
    @Override
    @Async
    public void sendVerificationEmail(String email, String nickname, String verificationCode) {
        String subject = String.format("[%s] 信箱驗證碼", appName);
        
        // 使用 Thymeleaf 模板
        Context context = new Context();
        context.setVariable("nickname", nickname);
        context.setVariable("verificationCode", verificationCode);
        String content = templateEngine.process("verification-email", context);
        
        Map<String, Object> params = Map.of(
            "nickname", nickname,
            "verificationCode", verificationCode
        );
        
        sendEmail("VERIFICATION", email, nickname, subject, content, "verification-email", params, null, null);
    }
    
    @Override
    @Async
    public void sendPasswordResetEmail(String email, String nickname, String resetToken) {
        String subject = String.format("[%s] 密碼重設請求", appName);
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        
        // 使用 Thymeleaf 模板
        Context context = new Context();
        context.setVariable("nickname", nickname);
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("resetToken", resetToken);
        context.setVariable("expiryMinutes", "30");
        String content = templateEngine.process("password-reset-email", context);
        
        Map<String, Object> params = Map.of(
            "nickname", nickname,
            "resetUrl", resetUrl,
            "resetToken", resetToken
        );
        
        sendEmail("PASSWORD_RESET", email, nickname, subject, content, "password-reset-email", params, null, null);
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
        
        sendEmail("ORDER", email, nickname, subject, content, "order_notification", params, "ORDER", orderNumber);
    }
    
    @Override
    @Async
    public void sendNotification(String email, String nickname, String subject, String content) {
        sendEmail("NOTIFICATION", email, nickname, subject, content, null, null, null, null);
    }
    
    @Override
    @Async
    public void sendInitialPasswordEmail(String to, String displayName, String initialPassword) {
        sendInitialPasswordEmailSync(to, displayName, initialPassword);
    }

    @Override
    public void sendInitialPasswordEmailSync(String to, String displayName, String initialPassword) {
        String subject = String.format("[%s] 後台帳號初始密碼", appName);

        Context context = new Context();
        context.setVariable("displayName", displayName);
        context.setVariable("initialPassword", initialPassword);
        context.setVariable("loginUrl", frontendUrl + "/admin/login");
        String content = templateEngine.process("initial-password-email", context);

        Map<String, Object> params = Map.of(
            "displayName", displayName,
            "initialPassword", initialPassword
        );

        sendEmailOrThrow("INITIAL_PASSWORD", to, displayName, subject, content,
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
        String safeLoginUrl = loginUrl != null && !loginUrl.isBlank() ? loginUrl : frontendUrl + "/login";
        String subject = String.format("[%s] 臨時密碼通知", appName);

        Context context = new Context();
        context.setVariable("displayName", safeDisplayName);
        context.setVariable("temporaryPassword", temporaryPassword);
        context.setVariable("loginUrl", safeLoginUrl);
        context.setVariable("scene", safeScene);
        String content = templateEngine.process("temporary-password-email", context);

        Map<String, Object> params = Map.of(
            "displayName", safeDisplayName,
            "temporaryPassword", temporaryPassword,
            "loginUrl", safeLoginUrl,
            "scene", safeScene
        );

        sendEmailOrThrow("TEMP_PASSWORD", to, safeDisplayName, subject, content,
                "temporary-password-email", params, null, null);
    }

    @Override
    public void retryFailedEmails() {
        List<EmailLog> failedEmails = emailLogRepository.selectPendingForRetry("FAILED", 3, 10);
        
        for (EmailLog emailLog : failedEmails) {
            try {
                doSendEmail(emailLog.getToEmail(), emailLog.getSubject(), emailLog.getContent());
                
                emailLog.setStatus("SENT");
                emailLog.setSentAt(LocalDateTime.now());
                emailLog.setRetryCount(emailLog.getRetryCount() + 1);
                emailLog.setUpdatedAt(LocalDateTime.now());
                emailLogRepository.updateStatus(emailLog);
                
                log.info("✅ 重試郵件發送成功: id={}, to={}", emailLog.getId(), emailLog.getToEmail());
            } catch (Exception e) {
                emailLog.setRetryCount(emailLog.getRetryCount() + 1);
                emailLog.setErrorMessage(e.getMessage());
                emailLog.setUpdatedAt(LocalDateTime.now());
                emailLogRepository.updateStatus(emailLog);
                
                log.error("❌ 重試郵件發送失敗: id={}, to={}, error={}", 
                         emailLog.getId(), emailLog.getToEmail(), e.getMessage());
            }
        }
    }
    
    private void sendEmail(String emailType, String toEmail, String toName, String subject, 
                          String content, String templateName, Map<String, Object> templateParams,
                          String relatedType, String relatedId) {
        sendEmailInternal(emailType, toEmail, toName, subject, content, templateName,
            templateParams, relatedType, relatedId, false);
        }

        private void sendEmailOrThrow(String emailType, String toEmail, String toName, String subject,
                      String content, String templateName, Map<String, Object> templateParams,
                      String relatedType, String relatedId) {
        sendEmailInternal(emailType, toEmail, toName, subject, content, templateName,
            templateParams, relatedType, relatedId, true);
        }

        private void sendEmailInternal(String emailType, String toEmail, String toName, String subject,
                       String content, String templateName, Map<String, Object> templateParams,
                       String relatedType, String relatedId, boolean throwOnFailure) {
        // 建立郵件記錄
        EmailLog emailLog = new EmailLog();
        emailLog.setId(UUID.randomUUID().toString());
        emailLog.setEmailType(emailType);
        emailLog.setToEmail(toEmail);
        emailLog.setToName(toName);
        emailLog.setSubject(subject);
        emailLog.setContent(content);
        emailLog.setTemplateName(templateName);
        emailLog.setStatus("PENDING");
        emailLog.setRetryCount(0);
        emailLog.setRelatedType(relatedType);
        emailLog.setRelatedId(relatedId);
        emailLog.setCreatedAt(LocalDateTime.now());
        emailLog.setUpdatedAt(LocalDateTime.now());
        
        try {
            if (templateParams != null) {
                emailLog.setTemplateParams(objectMapper.writeValueAsString(templateParams));
            }
        } catch (Exception e) {
            log.warn("郵件參數序列化失敗", e);
        }
        
        emailLogMapper.insert(emailLog);
        
        // 發送郵件
        try {
            doSendEmail(toEmail, subject, content);
            
            emailLog.setStatus("SENT");
            emailLog.setSentAt(LocalDateTime.now());
            emailLog.setUpdatedAt(LocalDateTime.now());
            emailLogRepository.updateStatus(emailLog);
            
            log.info("✅ 郵件發送成功: type={}, to={}, subject={}", emailType, toEmail, subject);
        } catch (Exception e) {
            emailLog.setStatus("FAILED");
            emailLog.setErrorMessage(e.getMessage());
            emailLog.setUpdatedAt(LocalDateTime.now());
            emailLogRepository.updateStatus(emailLog);
            
            log.error("❌ 郵件發送失敗: type={}, to={}, error={}", emailType, toEmail, e.getMessage());

            if (throwOnFailure) {
                throw new IllegalStateException("郵件發送失敗，請稍後再試", e);
            }
        }
    }
    
    private void doSendEmail(String to, String subject, String htmlContent) throws MessagingException {
        if (fromEmail == null || fromEmail.isEmpty()) {
            log.error("❌ 郵件發送功能未啟用（未設定 SMTP）");
            throw new IllegalStateException("SMTP 未設定，無法寄送郵件");
        }
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }
    
    private String buildVerificationEmailContent(String nickname, String verificationCode) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .code { font-size: 32px; font-weight: bold; color: #667eea; text-align: center; padding: 20px; background: white; border-radius: 8px; margin: 20px 0; letter-spacing: 5px; }
                    .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <p>親愛的 <strong>%s</strong>，您好！</p>
                        <p>感謝您註冊 %s。請使用以下驗證碼完成信箱驗證：</p>
                        <div class="code">%s</div>
                        <p>此驗證碼將於 <strong>30 分鐘</strong>後失效。</p>
                        <p>如果您沒有進行此操作，請忽略此郵件。</p>
                    </div>
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆。</p>
                        <p>&copy; %d %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, appName, nickname, appName, verificationCode, 
            LocalDateTime.now().getYear(), appName);
    }
    
    private String buildPasswordResetEmailContent(String nickname, String resetUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .button { display: inline-block; background: #f5576c; color: white; padding: 15px 30px; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 20px; color: #888; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>密碼重設請求</h1>
                    </div>
                    <div class="content">
                        <p>親愛的 <strong>%s</strong>，您好！</p>
                        <p>我們收到了您的密碼重設請求。請點擊下方按鈕重設您的密碼：</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">重設密碼</a>
                        </p>
                        <p>或複製以下連結到瀏覽器：</p>
                        <p style="word-break: break-all; color: #667eea;">%s</p>
                        <p>此連結將於 <strong>1 小時</strong>後失效。</p>
                        <p>如果您沒有進行此操作，請忽略此郵件並確保帳戶安全。</p>
                    </div>
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆。</p>
                        <p>&copy; %d %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, nickname, resetUrl, resetUrl,
            LocalDateTime.now().getYear(), appName);
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
