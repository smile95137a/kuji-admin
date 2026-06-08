package com.group.admin.service.impl;

import com.group.admin.service.MailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * SMTP 郵件傳輸實作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:${spring.mail.username:}}")
    private String fromEmail;

    @Override
    public void sendHtml(String to, String subject, String htmlContent) {
        if (fromEmail == null || fromEmail.isEmpty()) {
            log.error("❌ 郵件發送功能未啟用（未設定 SMTP）");
            throw new IllegalStateException("SMTP 未設定，無法寄送郵件");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("建立郵件內容失敗", e);
        }
    }
}