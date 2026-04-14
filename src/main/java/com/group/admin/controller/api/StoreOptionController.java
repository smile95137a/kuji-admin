package com.group.admin.controller.api;

import com.group.admin.res.common.EnumOption;
import com.group.admin.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台店家選項 Controller
 * 
 * <p>提供店家列表供前端選擇使用（無需登入）</p>
 * <p>僅返回啟用（ACTIVE）的店家</p>
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "前台店家選項", description = "提供店家列表供前台使用（無需登入）")
public class StoreOptionController {

    private final StoreService storeService;

    /**
     * 取得所有啟用的店家選項（前台專用）
     */
    @GetMapping("/options")
    @Operation(summary = "取得店家選項（前台）", description = "返回所有啟用的店家，格式：{ label: 店家名稱, value: 店家ID }")
    public ResponseEntity<List<EnumOption>> getStoreOptions() {
        
        log.info("📋 [前台] 取得店家選項列表");
        
        List<EnumOption> options = storeService.getAllActiveStoreOptions();
        
        log.info("✅ [前台] 返回 {} 個店家選項", options.size());
        return ResponseEntity.ok(options);
    }
}
