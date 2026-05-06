package com.group.admin.controller.api;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.consumption.ConsumptionRecordCondition;
import com.group.admin.res.PageResult;
import com.group.admin.res.consumption.ConsumptionRecordRes;
import com.group.admin.service.ConsumptionRecordService;
import com.group.admin.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 前台消費紀錄 Controller
 * 
 * <p>提供前台用戶查詢自己的消費紀錄（需登入）</p>
 * <p>消費紀錄僅包含：金幣抽獎、紅利抽獎、運費支付</p>
 * <p>⚠️ 儲值記錄不屬於消費紀錄</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/consumption-records")
@RequiredArgsConstructor
@Tag(name = "前台-消費紀錄", description = "用戶消費紀錄查詢（需登入）")
public class ConsumptionRecordController {

    private final ConsumptionRecordService consumptionRecordService;

    /**
     * 查詢我的消費紀錄
     * 
     * <p>支援條件篩選：消費類型、關鍵字、時間範圍</p>
     * <p>不帶任何參數時返回全部紀錄</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查詢我的消費紀錄", description = "查詢當前用戶的消費紀錄，支援動態條件查詢")
    public ResponseEntity<PageResult<ConsumptionRecordRes>> getMyConsumptionRecords(
            @RequestBody(required = false)
            @Parameter(description = "查詢條件（可選）")
            QueryReq<ConsumptionRecordCondition> req) {
        
        String userId = SecurityUtils.getCurrentUserId();
        log.info("📋 [前台] 查詢我的消費紀錄: userId={}", userId);
        
        PageResult<ConsumptionRecordRes> results = consumptionRecordService.getMyConsumptions(userId, req);
        return ResponseEntity.ok(results);
    }
}
