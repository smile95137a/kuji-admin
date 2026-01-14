package com.group.admin.controller.admin;

import com.group.admin.req.common.QueryReq;
import com.group.admin.req.user.FrontendUserCondition;
import com.group.admin.req.user.FrontendUserUpdateReq;
import com.group.admin.res.user.FrontendUserRes;
import com.group.admin.service.FrontendUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台會員管理 API（後台使用）
 * 
 * 路由：/admin/frontend-users/**
 * 權限：所有後台角色都可以查看和編輯
 * 
 * @author KUJI System
 * @since 1.0.0
 */
@RestController
@RequestMapping("/admin/frontend-users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "前台會員管理", description = "後台管理前台會員 API")
public class AdminFrontendUserController {
    
    private final FrontendUserService frontendUserService;
    
    /**
     * 查詢前台會員列表
     * 
     * ✅ 所有後台角色都可以查看
     * ✅ 不過濾店家（全部會員）
     * ✅ 排除已刪除的會員
     * 
     * @param req 查詢請求（可選）
     * @return 會員列表
     */
    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "查詢前台會員列表", description = "查詢所有前台會員（不過濾店家）")
    public ResponseEntity<List<FrontendUserRes>> queryUsers(
            @RequestBody(required = false) QueryReq<FrontendUserCondition> req) {
        
        log.info("🔍 查詢前台會員列表: req={}", req);
        
        List<FrontendUserRes> result = frontendUserService.queryUsers(req);
        
        log.info("✅ 查詢成功: 共 {} 筆", result.size());
        return ResponseEntity.ok(result);
    }
    
    /**
     * 取得會員詳情
     * 
     * @param id 會員 ID
     * @return 會員詳情
     */
    @GetMapping("/{id:[a-f0-9\\-]{36}}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "取得會員詳情", description = "查詢單一會員詳細資訊")
    public ResponseEntity<FrontendUserRes> getUser(@PathVariable String id) {
        
        log.info("🔍 查詢會員詳情: userId={}", id);
        
        FrontendUserRes result = frontendUserService.getUserById(id);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 更新會員資訊
     * 
     * @param id 會員 ID
     * @param req 更新請求
     * @return 更新後的會員資訊
     */
    @PutMapping("/{id:[a-f0-9\\-]{36}}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER', 'STORE_EDITOR')")
    @Operation(summary = "更新會員資訊", description = "編輯會員資料")
    public ResponseEntity<FrontendUserRes> updateUser(
            @PathVariable String id,
            @Valid @RequestBody FrontendUserUpdateReq req) {
        
        log.info("✏️ 更新會員資訊: userId={}, req={}", id, req);
        
        FrontendUserRes result = frontendUserService.updateUser(id, req);
        
        log.info("✅ 更新成功");
        return ResponseEntity.ok(result);
    }
    
    // ❌ 已移除 deleteUser 方法，只保留停用功能
    // 若需要刪除會員，請使用停用功能
    
    /**
     * 啟用會員
     * 
     * @param id 會員 ID
     * @return 成功訊息
     */
    @PostMapping("/{id:[a-f0-9\\-]{36}}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "啟用會員", description = "將會員設為 ACTIVE 狀態")
    public ResponseEntity<Void> activateUser(@PathVariable String id) {
        
        log.info("✅ 啟用會員: userId={}", id);
        
        frontendUserService.activateUser(id);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 停用會員
     * 
     * @param id 會員 ID
     * @return 成功訊息
     */
    @PostMapping("/{id:[a-f0-9\\-]{36}}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "停用會員", description = "將會員設為 INACTIVE 狀態")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        
        log.info("🔒 停用會員: userId={}", id);
        
        frontendUserService.deactivateUser(id);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * 暫停會員使用
     * 
     * @param id 會員 ID
     * @return 成功訊息
     */
    @PostMapping("/{id:[a-f0-9\\-]{36}}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_OWNER')")
    @Operation(summary = "暫停會員", description = "將會員設為 SUSPENDED 狀態（暫停使用）")
    public ResponseEntity<Void> suspendUser(@PathVariable String id) {
        
        log.info("⏸️ 暫停會員: userId={}", id);
        
        frontendUserService.suspendUser(id);
        
        return ResponseEntity.ok().build();
    }
}
