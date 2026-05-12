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
import java.util.Locale;
import java.util.Map;

public final class GoMyPaySupport {

    public static final String PAYMENT_METHOD_CREDIT_CARD = "CREDIT_CARD";
    public static final String PAYMENT_METHOD_BANK_TRANSFER = "BANK_TRANSFER";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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
        throw new BusinessException("不支援的付款方式：" + paymentMethod);
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

    public static String computeCallbackChecksum(Map<String, String> params, GoMyPayProperties properties) {
        String customerId = safe(properties.getVerifyCustomerId(), properties.getShopId());
        String secret = safe(properties.getHashKey());
        String result = firstNonBlank(params, "result");
        String merchantOrderNo = firstNonBlank(params, "e_orderno", "Order_No");
        String amount = firstNonBlank(params, "PayAmount", "e_money", "Amount");
        String gatewayOrderId = firstNonBlank(params, "OrderID");

        if (result == null || merchantOrderNo == null || amount == null || gatewayOrderId == null || customerId.isBlank() || secret.isBlank()) {
            throw new BusinessException("GoMyPay callback 資料不完整，無法驗簽");
        }

        return md5Hex(result + merchantOrderNo + customerId + amount + gatewayOrderId + secret);
    }

    public static void verifyCallback(Map<String, String> params, GoMyPayProperties properties) {
        String actual = firstNonBlank(params, "str_check", "Str_Check");
        if (actual == null || actual.isBlank()) {
            throw new BusinessException("GoMyPay callback 缺少 str_check");
        }

        String expected = computeCallbackChecksum(params, properties);
        if (!expected.equalsIgnoreCase(actual.trim())) {
            throw new BusinessException("GoMyPay callback 驗簽失敗");
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
