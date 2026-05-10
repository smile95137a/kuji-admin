package com.group.admin.controller.admin;

import com.group.admin.req.systemconfig.SystemConfigCreateReq;
import com.group.admin.req.systemconfig.SystemConfigUpdateReq;
import com.group.admin.res.systemconfig.SystemConfigRes;
import com.group.admin.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/system-config")
@RequiredArgsConstructor
@Tag(name = "後台系統參數管理", description = "系統參數 CRUD API")
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查詢系統參數", description = "可選擇依 group 或 configGroup 篩選")
    public ResponseEntity<List<SystemConfigRes>> list(
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String configGroup) {
        String targetGroup = (configGroup != null && !configGroup.isBlank()) ? configGroup : group;
        log.info("⚙️ 查詢系統參數: group={}, configGroup={}, targetGroup={}", group, configGroup, targetGroup);
        if (targetGroup == null || targetGroup.isBlank()) {
            return ResponseEntity.ok(systemConfigService.listAll());
        }
        return ResponseEntity.ok(systemConfigService.listByGroup(targetGroup));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "新增系統參數")
    public ResponseEntity<SystemConfigRes> create(@Valid @RequestBody SystemConfigCreateReq req) {
        log.info("➕ 新增系統參數: key={}", req.getConfigKey());
        return ResponseEntity.ok(systemConfigService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新系統參數")
    public ResponseEntity<SystemConfigRes> update(@PathVariable String id,
                                                   @Valid @RequestBody SystemConfigUpdateReq req) {
        log.info("✏️ 更新系統參數: id={}", id);
        return ResponseEntity.ok(systemConfigService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除系統參數")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("🗑️ 刪除系統參數: id={}", id);
        systemConfigService.delete(id);
        return ResponseEntity.ok().build();
    }
}
