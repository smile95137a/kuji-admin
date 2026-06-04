package com.group.admin.controller.admin;

import com.group.admin.condition.BusinessEventLogCondition;
import com.group.admin.entity.BusinessEventLog;
import com.group.admin.entity.LogAdminAction;
import com.group.admin.entity.LogAuth;
import com.group.admin.entity.LogDraw;
import com.group.admin.entity.LogOrder;
import com.group.admin.entity.LogRecharge;
import com.group.admin.example.LogAdminActionExample;
import com.group.admin.example.LogAuthExample;
import com.group.admin.example.LogDrawExample;
import com.group.admin.example.LogOrderExample;
import com.group.admin.example.LogRechargeExample;
import com.group.admin.mapper.LogAdminActionMapper;
import com.group.admin.mapper.LogAuthMapper;
import com.group.admin.mapper.LogDrawMapper;
import com.group.admin.mapper.LogOrderMapper;
import com.group.admin.mapper.LogRechargeMapper;
import com.group.admin.res.systemlog.AdminSystemLogItemRes;
import com.group.admin.service.BusinessEventLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/admin/system-log")
@RequiredArgsConstructor
@Tag(name = "系統日誌", description = "後台登入、操作與重要業務事件查詢")
public class AdminSystemLogController {

    private static final List<String> BUSINESS_TYPES = List.of(
            BusinessEventLogService.EVENT_PAYMENT,
            BusinessEventLogService.EVENT_LOGISTICS,
            BusinessEventLogService.EVENT_ORDER_STATUS,
            BusinessEventLogService.EVENT_WALLET
    );

    private final LogAuthMapper logAuthMapper;
    private final LogAdminActionMapper logAdminActionMapper;
    private final LogRechargeMapper logRechargeMapper;
    private final LogOrderMapper logOrderMapper;
    private final LogDrawMapper logDrawMapper;
    private final BusinessEventLogService businessEventLogService;

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "依日誌類型查詢")
    public ResponseEntity<?> listByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "200") int limit) {

        String normalizedType = type.toUpperCase();
        int safeLimit = safeLimit(limit);
        log.info("查詢系統日誌: type={}, limit={}", normalizedType, safeLimit);

        return switch (normalizedType) {
            case "LOGIN" -> ResponseEntity.ok(limitRows(loginLogs(null, null), safeLimit));
            case "ADMIN_ACTION" -> ResponseEntity.ok(limitRows(adminActionLogs(null, null), safeLimit));
            case "RECHARGE" -> ResponseEntity.ok(limitRows(rechargeLogs(null, null), safeLimit));
            case "ORDER" -> ResponseEntity.ok(limitRows(orderLogs(null, null), safeLimit));
            case "DRAW" -> ResponseEntity.ok(limitRows(drawLogs(null, null), safeLimit));
            case "PAYMENT", "LOGISTICS", "ORDER_STATUS", "WALLET" -> {
                BusinessEventLogCondition condition = new BusinessEventLogCondition();
                condition.setEventType(normalizedType);
                yield ResponseEntity.ok(businessEventLogService.find(condition, safeLimit)
                        .stream()
                        .map(this::toBusinessEventLogItem)
                        .toList());
            }
            default -> ResponseEntity.badRequest().body("不支援的日誌類型: " + type);
        };
    }

    @GetMapping("/login")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查詢登入日誌")
    public ResponseEntity<List<LogAuth>> listLoginLogs(@RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(limitRows(loginLogs(null, null), safeLimit(limit)));
    }

    @GetMapping("/admin-action")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查詢後台操作日誌")
    public ResponseEntity<List<LogAdminAction>> listAdminActionLogs(@RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(limitRows(adminActionLogs(null, null), safeLimit(limit)));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "依日期區間查詢所有系統日誌")
    public ResponseEntity<List<AdminSystemLogItemRes>> listByDateRange(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "200") int limit) {

        int safeLimit = safeLimit(limit);
        BusinessEventLogCondition condition = new BusinessEventLogCondition();
        condition.setStartAt(start);
        condition.setEndAt(end);

        List<AdminSystemLogItemRes> businessLogs = businessEventLogService.find(condition, safeLimit)
                .stream()
                .map(this::toBusinessEventLogItem)
                .toList();

        List<AdminSystemLogItemRes> rows = Stream.of(
                        loginLogs(start, end).stream().map(this::toLoginLogItem),
                        adminActionLogs(start, end).stream().map(this::toAdminActionLogItem),
                        rechargeLogs(start, end).stream().map(this::toRechargeLogItem),
                        orderLogs(start, end).stream().map(this::toOrderLogItem),
                        drawLogs(start, end).stream().map(this::toDrawLogItem),
                        businessLogs.stream()
                )
                .flatMap(stream -> stream)
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .limit(safeLimit)
                .toList();

        return ResponseEntity.ok(rows);
    }

    private List<LogAuth> loginLogs(LocalDateTime start, LocalDateTime end) {
        LogAuthExample example = new LogAuthExample();
        LogAuthExample.Criteria criteria = example.createCriteria();
        if (start != null) {
            criteria.andCreatedAtGreaterThanOrEqualTo(start);
        }
        if (end != null) {
            criteria.andCreatedAtLessThanOrEqualTo(end);
        }
        example.setOrderByClause("created_at DESC");
        return logAuthMapper.selectByExample(example);
    }

    private List<LogAdminAction> adminActionLogs(LocalDateTime start, LocalDateTime end) {
        LogAdminActionExample example = new LogAdminActionExample();
        LogAdminActionExample.Criteria criteria = example.createCriteria();
        if (start != null) {
            criteria.andCreatedAtGreaterThanOrEqualTo(start);
        }
        if (end != null) {
            criteria.andCreatedAtLessThanOrEqualTo(end);
        }
        example.setOrderByClause("created_at DESC");
        return logAdminActionMapper.selectByExample(example);
    }

    private List<LogRecharge> rechargeLogs(LocalDateTime start, LocalDateTime end) {
        LogRechargeExample example = new LogRechargeExample();
        LogRechargeExample.Criteria criteria = example.createCriteria();
        if (start != null) {
            criteria.andCreatedAtGreaterThanOrEqualTo(start);
        }
        if (end != null) {
            criteria.andCreatedAtLessThanOrEqualTo(end);
        }
        example.setOrderByClause("created_at DESC");
        return logRechargeMapper.selectByExample(example);
    }

    private List<LogOrder> orderLogs(LocalDateTime start, LocalDateTime end) {
        LogOrderExample example = new LogOrderExample();
        LogOrderExample.Criteria criteria = example.createCriteria();
        if (start != null) {
            criteria.andCreatedAtGreaterThanOrEqualTo(start);
        }
        if (end != null) {
            criteria.andCreatedAtLessThanOrEqualTo(end);
        }
        example.setOrderByClause("created_at DESC");
        return logOrderMapper.selectByExample(example);
    }

    private List<LogDraw> drawLogs(LocalDateTime start, LocalDateTime end) {
        LogDrawExample example = new LogDrawExample();
        LogDrawExample.Criteria criteria = example.createCriteria();
        if (start != null) {
            criteria.andCreatedAtGreaterThanOrEqualTo(start);
        }
        if (end != null) {
            criteria.andCreatedAtLessThanOrEqualTo(end);
        }
        example.setOrderByClause("created_at DESC");
        return logDrawMapper.selectByExample(example);
    }

    private AdminSystemLogItemRes toLoginLogItem(LogAuth row) {
        return AdminSystemLogItemRes.builder()
                .id(row.getId())
                .logType("LOGIN")
                .userId(row.getUserId())
                .userType(row.getUserType())
                .email(row.getEmail())
                .loginMethod(row.getLoginMethod())
                .result(row.getResult())
                .errorMessage(row.getErrorMessage())
                .ip(row.getIp())
                .userAgent(row.getUserAgent())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private AdminSystemLogItemRes toAdminActionLogItem(LogAdminAction row) {
        return AdminSystemLogItemRes.builder()
                .id(row.getId())
                .logType("ADMIN_ACTION")
                .adminId(row.getAdminId())
                .adminEmail(row.getAdminEmail())
                .adminRole(row.getAdminRole())
                .targetType(row.getTargetType())
                .targetId(row.getTargetId())
                .targetName(row.getTargetName())
                .action(row.getAction())
                .beforeSnapshot(row.getBeforeSnapshot())
                .afterSnapshot(row.getAfterSnapshot())
                .result(row.getResult())
                .errorMessage(row.getErrorMessage())
                .ip(row.getIp())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private AdminSystemLogItemRes toBusinessEventLogItem(BusinessEventLog row) {
        return AdminSystemLogItemRes.builder()
                .id(row.getId())
                .logType(row.getEventType())
                .userId(row.getUserId())
                .actorType(row.getActorType())
                .actorId(row.getActorId())
                .actorName(row.getActorName())
                .targetType(row.getTargetType())
                .targetId(row.getTargetId())
                .targetNo(row.getTargetNo())
                .orderId(row.getOrderId())
                .rechargeId(row.getRechargeId())
                .walletTransactionId(row.getWalletTransactionId())
                .externalProvider(row.getExternalProvider())
                .externalRef(row.getExternalRef())
                .paymentMethod(row.getPaymentMethod())
                .amount(row.getAmount())
                .beforeStatus(row.getBeforeStatus())
                .afterStatus(row.getAfterStatus())
                .action(row.getAction())
                .beforeSnapshot(row.getBeforeSnapshot())
                .afterSnapshot(row.getAfterSnapshot())
                .callbackSummary(row.getCallbackSummary())
                .rawPayloadHash(row.getRawPayloadHash())
                .result(row.getResult())
                .errorMessage(row.getErrorMessage())
                .ip(row.getIp())
                .userAgent(row.getUserAgent())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private AdminSystemLogItemRes toRechargeLogItem(LogRecharge row) {
        return AdminSystemLogItemRes.builder()
                .id(row.getId())
                .logType("RECHARGE")
                .userId(row.getUserId())
                .rechargeId(row.getRechargeId())
                .targetType("RECHARGE")
                .targetId(row.getRechargeId())
                .targetName(row.getPlanName())
                .externalProvider("GoMyPay")
                .externalRef(row.getPaymentGatewayRef())
                .paymentMethod(row.getPaymentMethod())
                .amount(row.getAmount())
                .afterSnapshot(rechargeSnapshot(row))
                .result(row.getResult())
                .errorMessage(row.getErrorMessage())
                .ip(row.getIp())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private AdminSystemLogItemRes toOrderLogItem(LogOrder row) {
        return AdminSystemLogItemRes.builder()
                .id(row.getId())
                .logType("ORDER")
                .actorType(row.getOperatorType())
                .actorId(row.getOperatorId())
                .targetType("ORDER")
                .targetId(row.getOrderId())
                .orderId(row.getOrderId())
                .action(row.getAction())
                .externalRef(row.getTrackingNumber())
                .amount(row.getTotalAmount())
                .afterSnapshot(orderSnapshot(row))
                .result(row.getResult())
                .errorMessage(row.getErrorMessage())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private AdminSystemLogItemRes toDrawLogItem(LogDraw row) {
        return AdminSystemLogItemRes.builder()
                .id(row.getId())
                .logType("DRAW")
                .userId(row.getUserId())
                .targetType("LOTTERY")
                .targetId(row.getLotteryId())
                .targetName(row.getLotteryTitle())
                .action("DRAW")
                .externalRef(row.getTicketId())
                .afterSnapshot(drawSnapshot(row))
                .result(row.getResult())
                .errorMessage(row.getErrorMessage())
                .createdAt(row.getCreatedAt())
                .build();
    }

    private String rechargeSnapshot(LogRecharge row) {
        return "{"
                + jsonPair("planId", row.getPlanId()) + ","
                + jsonPair("planName", row.getPlanName()) + ","
                + "\"goldAdded\":" + nullableNumber(row.getGoldAdded()) + ","
                + "\"bonusAdded\":" + nullableNumber(row.getBonusAdded())
                + "}";
    }

    private String orderSnapshot(LogOrder row) {
        return "{"
                + "\"prizeBoxCount\":" + nullableNumber(row.getPrizeBoxCount()) + ","
                + jsonPair("trackingNumber", row.getTrackingNumber())
                + "}";
    }

    private String drawSnapshot(LogDraw row) {
        return "{"
                + jsonPair("category", row.getCategory()) + ","
                + jsonPair("playMode", row.getPlayMode()) + ","
                + jsonPair("gameMode", row.getGameMode()) + ","
                + "\"ticketNumber\":" + nullableNumber(row.getTicketNumber()) + ","
                + jsonPair("prizeLevel", row.getPrizeLevel()) + ","
                + jsonPair("prizeName", row.getPrizeName()) + ","
                + "\"deductedGold\":" + nullableNumber(row.getDeductedGold()) + ","
                + "\"deductedBonus\":" + nullableNumber(row.getDeductedBonus()) + ","
                + "\"durationMs\":" + nullableNumber(row.getDurationMs())
                + "}";
    }

    private <T> List<T> limitRows(List<T> rows, int limit) {
        return rows.size() > limit ? rows.subList(0, limit) : rows;
    }

    private int safeLimit(int limit) {
        if (limit <= 0) {
            return 200;
        }
        return Math.min(limit, 1000);
    }

    private String nullableNumber(Number value) {
        return value != null ? value.toString() : "null";
    }

    private String jsonPair(String key, String value) {
        return "\"" + escapeJson(key) + "\":" + (value == null ? "null" : "\"" + escapeJson(value) + "\"");
    }

    private String escapeJson(String value) {
        return value == null ? null : value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
