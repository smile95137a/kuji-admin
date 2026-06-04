package com.group.admin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.admin.condition.BusinessEventLogCondition;
import com.group.admin.entity.BusinessEventLog;
import com.group.admin.repository.BusinessEventLogRepository;
import com.group.admin.service.BusinessEventLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessEventLogServiceImpl implements BusinessEventLogService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 500;
    private static final int SNAPSHOT_MAX_LENGTH = 16_000;
    private static final List<String> SENSITIVE_KEYWORDS = List.of(
            "card", "pan", "cvv", "cvc", "password", "token", "secret", "key");

    private final BusinessEventLogRepository repository;
    private final ObjectMapper objectMapper;

    @Async("taskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(BusinessEventLog logRecord) {
        if (logRecord == null) {
            return;
        }
        try {
            BusinessEventLog safeLog = normalize(logRecord);
            repository.insert(safeLog);
        } catch (Exception ex) {
            log.warn("[BusinessEventLog] 寫入失敗: {}", ex.getMessage());
        }
    }

    @Override
    public void recordCallback(String eventType, String action, String result,
                               String targetType, String targetId, String targetNo,
                               String userId, String orderId, String rechargeId,
                               String externalProvider, String externalRef,
                               Long amount, String paymentMethod,
                               String beforeStatus, String afterStatus,
                               Map<String, String> callbackParams,
                               String errorMessage) {
        Map<String, String> maskedParams = maskCallbackParams(callbackParams);
        record(BusinessEventLog.builder()
                .eventType(eventType)
                .action(action)
                .result(result)
                .actorType("CALLBACK")
                .targetType(targetType)
                .targetId(targetId)
                .targetNo(targetNo)
                .userId(userId)
                .orderId(orderId)
                .rechargeId(rechargeId)
                .externalProvider(externalProvider)
                .externalRef(externalRef)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .callbackSummary(toJson(maskedParams))
                .rawPayloadHash(hashCanonical(callbackParams))
                .errorMessage(errorMessage)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessEventLog> find(BusinessEventLogCondition condition, int limit) {
        return repository.findByCondition(condition, limit);
    }

    @Override
    @Transactional
    public int cleanupBeforeDays(int days) {
        int safeDays = Math.max(days, 1);
        return repository.deleteBefore(LocalDateTime.now().minusDays(safeDays));
    }

    private BusinessEventLog normalize(BusinessEventLog source) {
        return BusinessEventLog.builder()
                .id(hasText(source.getId()) ? source.getId() : UUID.randomUUID().toString())
                .eventType(required(source.getEventType(), "SYSTEM"))
                .action(required(source.getAction(), "UNKNOWN"))
                .result(required(source.getResult(), RESULT_SUCCESS))
                .actorType(blankToNull(source.getActorType()))
                .actorId(blankToNull(source.getActorId()))
                .actorName(blankToNull(source.getActorName()))
                .targetType(blankToNull(source.getTargetType()))
                .targetId(blankToNull(source.getTargetId()))
                .targetNo(blankToNull(source.getTargetNo()))
                .userId(blankToNull(source.getUserId()))
                .orderId(blankToNull(source.getOrderId()))
                .rechargeId(blankToNull(source.getRechargeId()))
                .walletTransactionId(blankToNull(source.getWalletTransactionId()))
                .externalProvider(blankToNull(source.getExternalProvider()))
                .externalRef(blankToNull(source.getExternalRef()))
                .amount(source.getAmount())
                .paymentMethod(blankToNull(source.getPaymentMethod()))
                .beforeStatus(blankToNull(source.getBeforeStatus()))
                .afterStatus(blankToNull(source.getAfterStatus()))
                .beforeSnapshot(limitLength(source.getBeforeSnapshot(), SNAPSHOT_MAX_LENGTH))
                .afterSnapshot(limitLength(source.getAfterSnapshot(), SNAPSHOT_MAX_LENGTH))
                .callbackSummary(limitLength(source.getCallbackSummary(), SNAPSHOT_MAX_LENGTH))
                .rawPayloadHash(blankToNull(source.getRawPayloadHash()))
                .errorMessage(limitLength(source.getErrorMessage(), ERROR_MESSAGE_MAX_LENGTH))
                .ip(blankToNull(source.getIp()))
                .userAgent(limitLength(source.getUserAgent(), 500))
                .createdAt(source.getCreatedAt() != null ? source.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private Map<String, String> maskCallbackParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return Map.of();
        }
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> shouldMask(entry.getKey()) ? mask(entry.getValue()) : entry.getValue(),
                        (left, right) -> right,
                        LinkedHashMap::new));
    }

    private boolean shouldMask(String key) {
        String normalized = key == null ? "" : key.toLowerCase();
        return SENSITIVE_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private String mask(String value) {
        if (!hasText(value)) {
            return value;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        return "****" + trimmed.substring(trimmed.length() - 4);
    }

    private String hashCanonical(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }
        String canonical = params.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    private String required(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }

    private String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String limitLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
