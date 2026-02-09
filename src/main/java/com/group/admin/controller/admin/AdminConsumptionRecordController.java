package com.group.admin.controller.admin;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.consumption.ConsumptionRecordCondition;
import com.group.admin.res.consumption.ConsumptionRecordRes;
import com.group.admin.service.ConsumptionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 後台消費紀錄管理 Controller
 * 
 * <p>提供後台管理員查詢所有用戶的消費紀錄功能</p>
 * <p>消費紀錄僅包含：金幣抽獎、紅利抽獎、運費支付</p>
 * <p>⚠️ 儲值記錄不屬於消費紀錄</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/consumption-records")
@RequiredArgsConstructor
@Tag(name = "後台-消費紀錄管理", description = "所有用戶消費紀錄查詢（僅 Admin）")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConsumptionRecordController {

    private final ConsumptionRecordService consumptionRecordService;

    /**
     * 查詢所有消費紀錄
     * 
     * <p>支援動態條件查詢（用戶 ID、消費類型、訂單編號、時間範圍、關鍵字）</p>
     * <p>不帶任何參數時返回全部紀錄</p>
     */
    @PostMapping("/list")
    @Operation(summary = "查詢所有消費紀錄", description = "後台查詢所有用戶的消費紀錄，支援動態條件查詢")
    public ResponseEntity<List<ConsumptionRecordRes>> queryConsumptionRecords(
            @RequestBody(required = false)
            @Parameter(description = "查詢條件（可選）")
            QueryReq<ConsumptionRecordCondition> req) {
        
        log.info("📋 [後台] 查詢所有消費紀錄");
        List<ConsumptionRecordRes> results = consumptionRecordService.queryConsumptions(req);
        return ResponseEntity.ok(results);
    }
}
