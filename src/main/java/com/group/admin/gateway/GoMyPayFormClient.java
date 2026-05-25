package com.group.admin.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoMyPayFormClient {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public Map<String, String> postForm(String apiUrl, Map<String, String> params) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Accept", "application/json,text/plain,*/*")
                    .POST(HttpRequest.BodyPublishers.ofString(toFormBody(params), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("GoMyPay 連線失敗，HTTP " + response.statusCode());
            }
            Map<String, String> parsed = parseResponse(response.body());
            parsed.putIfAbsent("_raw", response.body());
            return parsed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("GoMyPay 連線中斷");
        } catch (IOException e) {
            log.warn("GoMyPay form post failed: {}", e.getMessage());
            throw new BusinessException("GoMyPay 連線失敗，請稍後再試");
        }
    }

    private String toFormBody(Map<String, String> params) {
        StringBuilder body = new StringBuilder();
        params.forEach((key, value) -> {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(encode(key)).append('=').append(encode(value));
        });
        return body.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseResponse(String body) throws IOException {
        Map<String, String> result = new LinkedHashMap<>();
        String trimmed = body == null ? "" : body.trim();
        if (trimmed.isBlank()) {
            return result;
        }

        if (trimmed.startsWith("{")) {
            Map<String, Object> raw = objectMapper.readValue(trimmed, MAP_TYPE);
            raw.forEach((key, value) -> result.put(key, value == null ? "" : String.valueOf(value)));
            return result;
        }

        for (String pair : trimmed.split("&")) {
            int idx = pair.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }
}
