package com.group.admin.controller.admin;

import com.group.admin.entity.SystemLog;
import com.group.admin.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系統日誌管理 API（後台）
 */
@Slf4j
@RestController
@RequestMapping("/admin/system-log")
@RequiredArgsConstructor
public class AdminSystemLogController {
    
    private final SystemLogService systemLogService;
    
    /**
     * 按類型查詢日誌
     */
    @GetMapping("/type/{logType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SystemLog>> getLogsByType(
            @PathVariable String logType,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(systemLogService.getLogsByType(logType, limit));
    }
    
    /**
     * 按使用者查詢日誌
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SystemLog>> getLogsByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(systemLogService.getLogsByUserId(userId, limit));
    }
    
    /**
     * 按時間範圍查詢日誌
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SystemLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(systemLogService.getLogsByDateRange(start, end, limit));
    }
    
    /**
     * 清除過期日誌
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> deleteOldLogs(@RequestParam(defaultValue = "90") int days) {
        int deleted = systemLogService.deleteOldLogs(days);
        return ResponseEntity.ok(deleted);
    }
}
