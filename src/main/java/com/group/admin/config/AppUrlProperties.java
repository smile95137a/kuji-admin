package com.group.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppUrlProperties {

    private String baseUrl = "http://localhost:3000";

    private String clientPath = "/client";

    private String adminPath = "/kuji";

    public String getClientBaseUrl() {
        return join(baseUrl, clientPath);
    }

    public String getAdminBaseUrl() {
        return join(baseUrl, adminPath);
    }

    public String getClientLoginUrl() {
        return join(getClientBaseUrl(), "/login");
    }

    public String getAdminLoginUrl() {
        return join(getAdminBaseUrl(), "/login");
    }

    public String getClientOAuth2CallbackUrl() {
        return join(getClientBaseUrl(), "/oauth2/callback");
    }

    private String join(String base, String path) {
        String normalizedBase = normalizeBase(base);
        String normalizedPath = normalizePath(path);
        if (normalizedPath.isEmpty()) {
            return normalizedBase;
        }
        return normalizedBase + normalizedPath;
    }

    private String normalizeBase(String value) {
        String text = value == null || value.isBlank() ? "http://localhost:3000" : value.trim();
        while (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private String normalizePath(String value) {
        if (value == null || value.isBlank() || "/".equals(value.trim())) {
            return "";
        }

        String text = value.trim();
        if (!text.startsWith("/")) {
            text = "/" + text;
        }
        while (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
