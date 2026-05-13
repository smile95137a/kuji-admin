package com.group.admin.service;

import java.util.Map;

/**
 * 郵件派送服務。
 * 負責郵件日誌、寄送流程、失敗處理與重試。
 */
public interface EmailDispatchService {

    void send(String emailType, String toEmail, String toName, String subject,
              String content, String templateName, Map<String, Object> templateParams,
              String relatedType, String relatedId, boolean throwOnFailure);

    void retryFailedEmails();
}