package com.group.admin.controller.api;

import com.group.admin.res.store.StoreRes;
import com.group.admin.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "前台 - 店家", description = "公開店家列表 API（無需登入）")
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/list")
    @Operation(summary = "公開店家列表", description = "取得所有啟用的店家（公開 API，不需登入）")
    public ResponseEntity<List<StoreRes>> getPublicStoreList(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "頁碼（從 0 開始）", example = "0")
            int page,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "每頁筆數", example = "20")
            int size) {
        log.info("📋 [前台] 查詢公開店家列表: page={}, size={}", page, size);
        List<StoreRes> stores = storeService.getPublicStoreList(page, size);
        return ResponseEntity.ok(stores);
    }
}
