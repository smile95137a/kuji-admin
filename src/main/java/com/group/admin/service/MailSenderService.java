package com.group.admin.service;

/**
 * 郵件傳輸服務。
 * 僅負責 SMTP 寄送，不承擔業務場景與日誌流程。
 */
public interface MailSenderService {

    /**
     * 寄送 HTML 郵件。
     */
    void sendHtml(String to, String subject, String htmlContent);
}