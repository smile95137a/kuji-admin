package com.group.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.entity.EmailLog;
import com.group.admin.mapper.EmailLogMapper;
import com.group.admin.repository.EmailLogRepository;
import com.group.admin.service.EmailDispatchService;
import com.group.admin.service.MailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 郵件派送與日誌管理實作。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDispatchServiceImpl implements EmailDispatchService {

    private final MailSenderService mailSenderService;
    private final EmailLogMapper emailLogMapper;
    private final EmailLogRepository emailLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void send(String emailType, String toEmail, String toName, String subject,
                     String content, String templateName, Map<String, Object> templateParams,
                     String relatedType, String relatedId, boolean throwOnFailure) {
        EmailLog emailLog = buildEmailLog(emailType, toEmail, toName, subject, content,
                templateName, relatedType, relatedId, templateParams);

        emailLogMapper.insert(emailLog);

        try {
            mailSenderService.sendHtml(toEmail, subject, content);

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

    @Override
    public void retryFailedEmails() {
        List<EmailLog> failedEmails = emailLogRepository.selectPendingForRetry("FAILED", 3, 10);

        for (EmailLog emailLog : failedEmails) {
            try {
                mailSenderService.sendHtml(emailLog.getToEmail(), emailLog.getSubject(), emailLog.getContent());

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

    private EmailLog buildEmailLog(String emailType, String toEmail, String toName, String subject,
                                   String content, String templateName, String relatedType,
                                   String relatedId, Map<String, Object> templateParams) {
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

        return emailLog;
    }
}