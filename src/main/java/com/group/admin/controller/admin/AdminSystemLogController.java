package com.group.admin.controller.admin;

import com.group.admin.entity.LogAdminAction;
import com.group.admin.entity.LogAuth;
import com.group.admin.example.LogAdminActionExample;
import com.group.admin.example.LogAuthExample;
import com.group.admin.mapper.LogAdminActionMapper;
import com.group.admin.mapper.LogAuthMapper;
import com.group.admin.res.systemlog.AdminSystemLogItemRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

/**
 * 後台系統日誌查詢 API
 *
 * <p>支援 type 篩選：</p>
 * <ul>
 *   <li>LOGIN — 查 log_auth（登入/登出紀錄）</li>
 *   <li>ADMIN_ACTION — 查 log_admin_action（後台操作紀錄）</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/admin/system-log")
@RequiredArgsConstructor
@Tag(name = "後台系統日誌", description = "查詢登入與操作日誌")
public class AdminSystemLogController {

    private final LogAuthMapper        logAuthMapper;
    private final LogAdminActionMapper logAdminActionMapper;

    /**
     * 依類型查詢日誌
     *
     * @param type  LOG / ADMIN_ACTION
     * @param limit 最多返回筆數，預設 200
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "依類型查詢系統日誌")
    public ResponseEntity<?> listByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "200") int limit) {

        log.info("🔍 查詢系統日誌: type={}, limit={}", type, limit);

        return switch (type.toUpperCase()) {
            case "LOGIN" -> {
                LogAuthExample ex = new LogAuthExample();
                ex.createCriteria();
                ex.setOrderByClause("created_at DESC");
                List<LogAuth> rows = logAuthMapper.selectByExample(ex);
                yield ResponseEntity.ok(rows.size() > limit ? rows.subList(0, limit) : rows);
            }
            case "ADMIN_ACTION" -> {
                LogAdminActionExample ex = new LogAdminActionExample();
                ex.createCriteria();
                ex.setOrderByClause("created_at DESC");
                List<LogAdminAction> rows = logAdminActionMapper.selectByExample(ex);
                yield ResponseEntity.ok(rows.size() > limit ? rows.subList(0, limit) : rows);
            }
            default -> ResponseEntity.badRequest().body("不支援的日誌類型: " + type + "（支援: LOGIN, ADMIN_ACTION）");
        };
    }

    /**
     * 查詢所有登入日誌（快捷路徑）
     */
    @GetMapping("/login")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查詢登入日誌")
    public ResponseEntity<List<LogAuth>> listLoginLogs(
            @RequestParam(defaultValue = "200") int limit) {
        LogAuthExample ex = new LogAuthExample();
        ex.createCriteria();
        ex.setOrderByClause("created_at DESC");
        List<LogAuth> rows = logAuthMapper.selectByExample(ex);
        return ResponseEntity.ok(rows.size() > limit ? rows.subList(0, limit) : rows);
    }

    /**
     * 查詢後台操作日誌（快捷路徑）
     */
    @GetMapping("/admin-action")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查詢後台操作日誌")
    public ResponseEntity<List<LogAdminAction>> listAdminActionLogs(
            @RequestParam(defaultValue = "200") int limit) {
        LogAdminActionExample ex = new LogAdminActionExample();
        ex.createCriteria();
        ex.setOrderByClause("created_at DESC");
        List<LogAdminAction> rows = logAdminActionMapper.selectByExample(ex);
        return ResponseEntity.ok(rows.size() > limit ? rows.subList(0, limit) : rows);
    }

    /**
     * 依日期區間查詢所有系統日誌，前端會再依 logType 分頁或篩選。
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "依日期區間查詢系統日誌")
    public ResponseEntity<List<AdminSystemLogItemRes>> listByDateRange(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(defaultValue = "200") int limit) {

        log.info("查詢系統日誌日期區間: start={}, end={}, limit={}", start, end, limit);

        LogAuthExample logAuthExample = new LogAuthExample();
        logAuthExample.createCriteria()
                .andCreatedAtGreaterThanOrEqualTo(start)
                .andCreatedAtLessThanOrEqualTo(end);
        logAuthExample.setOrderByClause("created_at DESC");

        LogAdminActionExample adminActionExample = new LogAdminActionExample();
        adminActionExample.createCriteria()
                .andCreatedAtGreaterThanOrEqualTo(start)
                .andCreatedAtLessThanOrEqualTo(end);
        adminActionExample.setOrderByClause("created_at DESC");

        List<AdminSystemLogItemRes> rows = Stream.concat(
                        logAuthMapper.selectByExample(logAuthExample).stream()
                                .map(this::toLoginLogItem),
                        logAdminActionMapper.selectByExample(adminActionExample).stream()
                                .map(this::toAdminActionLogItem)
                )
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .limit(Math.max(limit, 0))
                .toList();

        return ResponseEntity.ok(rows);
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
}
