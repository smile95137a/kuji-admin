package com.group.admin.service;

import java.util.Map;

/**
 * 郵件模板渲染服務。
 */
public interface MailTemplateService {

    /**
     * 使用模板名稱與變數渲染 HTML 內容。
     */
    String render(String templateName, Map<String, Object> variables);
}