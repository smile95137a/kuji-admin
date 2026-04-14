package com.group.admin.controller.api;

import com.group.admin.res.PageResult;
import com.group.admin.res.store.StoreDetailRes;
import com.group.admin.res.store.StoreListItemRes;
import com.group.admin.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "前台 - 店家", description = "公開店家列表 API（無需登入）")
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    @Operation(summary = "公開店家列表", description = "取得所有啟用的店家（分頁）")
    public ResponseEntity<PageResult<StoreListItemRes>> listEnabledStores(
            @RequestParam(defaultValue = "1")
            @Parameter(description = "頁碼（從 1 開始）", example = "1")
            int page,
            @RequestParam(defaultValue = "20")
            @Parameter(description = "每頁筆數", example = "20")
            int size) {
        log.info("📋 [前台] 查詢公開店家列表: page={}, size={}", page, size);
        PageResult<StoreListItemRes> result = storeService.listEnabledStores(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "店家詳情", description = "取得單一啟用店家的詳細資訊（含上架商品）")
    public ResponseEntity<StoreDetailRes> getStoreDetail(
            @PathVariable
            @Parameter(description = "店家 ID", example = "uuid-store-1")
            String id) {
        log.info("🔍 [前台] 查詢店家詳情: id={}", id);
        StoreDetailRes result = storeService.getPublicStoreDetail(id);
        return ResponseEntity.ok(result);
    }
}

