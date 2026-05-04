package com.group.admin.controller.admin;

import com.group.admin.entity.LogAdminAction;
import com.group.admin.entity.LogAuth;
import com.group.admin.example.LogAdminActionExample;
import com.group.admin.example.LogAuthExample;
import com.group.admin.mapper.LogAdminActionMapper;
import com.group.admin.mapper.LogAuthMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
