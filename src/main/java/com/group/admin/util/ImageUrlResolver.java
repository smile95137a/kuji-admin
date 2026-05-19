package com.group.admin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImageUrlResolver {

    private final String publicBaseUrl;
    private final String legacyBaseUrl;

    public ImageUrlResolver(
            @Value("${aws.s3.base-url:}") String publicBaseUrl,
            @Value("${aws.s3.legacy-base-url:https://test-ourkuji.s3.ap-northeast-1.amazonaws.com}") String legacyBaseUrl) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.legacyBaseUrl = trimTrailingSlash(legacyBaseUrl);
    }

    public String normalize(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }
        if (publicBaseUrl.isBlank() || legacyBaseUrl.isBlank()) {
            return imageUrl;
        }
        if (imageUrl.startsWith(publicBaseUrl + "/")) {
            return imageUrl;
        }
        if (imageUrl.startsWith(legacyBaseUrl + "/")) {
            return publicBaseUrl + imageUrl.substring(legacyBaseUrl.length());
        }
        return imageUrl;
    }

    public List<String> normalizeList(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return imageUrls;
        }
        return imageUrls.stream().map(this::normalize).toList();
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
