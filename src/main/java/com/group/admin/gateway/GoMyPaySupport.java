package com.group.admin.gateway;

import com.group.admin.config.GoMyPayProperties;
import com.group.admin.exception.BusinessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class GoMyPaySupport {

    public static final String PAYMENT_METHOD_CREDIT_CARD = "CREDIT_CARD";
    public static final String PAYMENT_METHOD_BANK_TRANSFER = "BANK_TRANSFER";

    private static final int MAX_MERCHANT_ORDER_NO_LENGTH = 25;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}]");

    private GoMyPaySupport() {
    }

    public static String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return PAYMENT_METHOD_CREDIT_CARD;
        }

        String normalized = paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (PAYMENT_METHOD_CREDIT_CARD.equals(normalized) || PAYMENT_METHOD_BANK_TRANSFER.equals(normalized)) {
            return normalized;
        }
        throw new BusinessException("不支援的 GoMyPay 付款方式: " + paymentMethod);
    }

    public static boolean isBankTransfer(String paymentMethod) {
        return PAYMENT_METHOD_BANK_TRANSFER.equals(normalizePaymentMethod(paymentMethod));
    }

    public static long normalizeAmount(BigDecimal amount) {
        return amount == null ? 0L : amount.setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public static String buildPayUrl(String apiUrl, Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl);
        params.forEach(builder::queryParam);
        return builder.build(false).encode(StandardCharsets.UTF_8).toUriString();
    }

    public static void validatePaymentRequestConfig(GoMyPayProperties properties, String returnUrl, String callbackUrl) {
        requireConfigured("GOMYPAY_API_URL", properties.getApiUrl(), false);
        requireConfigured("GOMYPAY_SHOP_ID", properties.getShopId(), false);
        requireConfigured("GOMYPAY_TRANSACTION_PASSWORD", requestTransactionPassword(properties), false);
        requireConfigured("GOMYPAY_RETURN_URL", returnUrl, true);
        requireConfigured("GOMYPAY_NOTIFY_URL", callbackUrl, true);
    }

    public static void validateMerchantOrderNo(String merchantOrderNo) {
        if (merchantOrderNo == null || merchantOrderNo.isBlank()) {
            throw new BusinessException("GoMyPay 缺少商戶訂單編號");
        }
        if (merchantOrderNo.trim().length() > MAX_MERCHANT_ORDER_NO_LENGTH) {
            throw new BusinessException("GoMyPay 商戶訂單編號不可超過 25 字元");
        }
    }

    public static String sanitizeBuyerName(String value, String fallback) {
        return sanitizeText(value, fallback, 40);
    }

    public static String sanitizeBuyerMemo(String value, String fallback) {
        return sanitizeText(value, fallback, 200);
    }

    public static String sanitizePhone(String value, String fallback) {
        String sanitized = value == null ? "" : value.replaceAll("[^0-9+\\-]", "").trim();
        return sanitized.isBlank() ? fallback : sanitized;
    }

    public static String sanitizeEmail(String value, String fallback) {
        String sanitized = safe(value).replaceAll("\\s+", "");
        return sanitized.isBlank() ? fallback : sanitized;
    }

    public static String computeRequestChecksum(String merchantOrderNo, long amount, GoMyPayProperties properties) {
        String secret = requestTransactionPassword(properties);
        validateMerchantOrderNo(merchantOrderNo);
        if (merchantOrderNo == null || merchantOrderNo.isBlank() || amount <= 0L || secret.isBlank()) {
            throw new BusinessException("GoMyPay 請求資料不足，無法產生 Str_Check");
        }
        return secret.trim();
    }

    public static String computeCallbackChecksum(Map<String, String> params, GoMyPayProperties properties) {
        String customerId = safe(properties.getVerifyCustomerId(), properties.getShopId());
        String secret = requestTransactionPassword(properties);
        String result = firstNonBlank(params, "result");
        String merchantOrderNo = firstNonBlank(params, "e_orderno", "Order_No", "orderNo", "MerchantOrderNo");
        String amount = firstNonBlank(params, "PayAmount", "e_money", "Amount");
        String gatewayOrderId = firstNonBlank(params, "OrderID", "TradeNo", "gatewayTradeNo", "Trade_No");

        if (result == null || merchantOrderNo == null || amount == null || gatewayOrderId == null || customerId.isBlank() || secret.isBlank()) {
            throw new BusinessException("GoMyPay callback 驗章資料不足");
        }

        return md5Hex(result + merchantOrderNo + customerId + amount + gatewayOrderId + secret);
    }

    public static String requestTransactionPassword(GoMyPayProperties properties) {
        return safe(properties.getTransactionPassword(), properties.getHashKey());
    }

    public static Map<String, String> toDebugParams(Map<String, String> params) {
        Map<String, String> debugParams = new LinkedHashMap<>();
        if (params == null) {
            return debugParams;
        }
        params.forEach((key, value) -> debugParams.put(key, maskSensitiveValue(key, value)));
        return debugParams;
    }

    public static void verifyCallback(Map<String, String> params, GoMyPayProperties properties) {
        String actual = firstNonBlank(params, "str_check", "Str_Check");
        if (actual == null || actual.isBlank()) {
            throw new BusinessException("GoMyPay callback 缺少 str_check");
        }

        String expected = computeCallbackChecksum(params, properties);
        if (!expected.equalsIgnoreCase(actual.trim())) {
            throw new BusinessException("GoMyPay callback 驗章失敗");
        }
    }

    public static LocalDateTime parsePaidAt(Map<String, String> params) {
        String date = firstNonBlank(params, "e_date");
        String time = firstNonBlank(params, "e_time");
        if (date == null || time == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.of(
                java.time.LocalDate.parse(date, DATE_FORMATTER),
                java.time.LocalTime.parse(time, TIME_FORMATTER)
        );
    }

    public static boolean isSuccess(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return "1".equals(normalized)
                || "Y".equals(normalized)
                || "TRUE".equals(normalized)
                || "SUCCESS".equals(normalized)
                || "PAID".equals(normalized)
                || "00".equals(normalized);
    }

    public static String firstNonBlank(Map<String, String> params, String... keys) {
        if (params == null) {
            return null;
        }
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    public static String safe(String value) {
        return value == null ? "" : value;
    }

    public static String safe(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? safe(fallback) : preferred;
    }

    private static String maskSensitiveValue(String key, String value) {
        if (value == null) {
            return null;
        }
        String normalizedKey = key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
        if ("str_check".equals(normalizedKey)) {
            return maskMiddle(value, 4, 4);
        }
        return value;
    }

    private static String maskMiddle(String value, int prefixLength, int suffixLength) {
        if (value.length() <= prefixLength + suffixLength) {
            return "*".repeat(Math.max(value.length(), 1));
        }
        return value.substring(0, prefixLength)
                + "*".repeat(value.length() - prefixLength - suffixLength)
                + value.substring(value.length() - suffixLength);
    }

    private static void requireConfigured(String key, String value, boolean urlRequired) {
        String normalized = safe(value).trim();
        if (normalized.isBlank()) {
            throw new BusinessException("GoMyPay 缺少必要設定: " + key);
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.contains("example.com") || lower.contains("placeholder") || normalized.contains("??")) {
            throw new BusinessException("GoMyPay 設定不可使用預設或占位值，請檢查 " + key);
        }
        if (urlRequired && !(lower.startsWith("http://") || lower.startsWith("https://"))) {
            throw new BusinessException("GoMyPay URL 設定格式錯誤: " + key);
        }
    }

    private static String sanitizeText(String value, String fallback, int maxLength) {
        String sanitized = safe(value, fallback)
                .replace("/browse/", " ")
                .replace("\\", " ")
                .replace("/", " ")
                .replace("\r", " ")
                .replace("\n", " ");
        sanitized = CONTROL_CHARS.matcher(sanitized).replaceAll(" ");
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        if (sanitized.isBlank()) {
            sanitized = fallback;
        }
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        return sanitized;
    }

    public static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("無法計算 GoMyPay checksum", e);
        }
    }
}
